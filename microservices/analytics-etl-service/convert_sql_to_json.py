#!/usr/bin/env python3
"""
Convert TDengine SQL from column-based format to JSON format
Usage: python convert_sql_to_json.py
"""

import re
from datetime import datetime, timedelta

def convert_insert_to_json(line):
    """Convert old column-based INSERT to JSON format"""
    # Pattern: INSERT INTO gaze_events_viewer_XXX USING gaze_events TAGS('session_end') VALUES (NOW - XXm, 'viewer_XXX', gaze_time, session_duration, interested, attention_rate, age, 'gender', 'emotion', 'ad_name');
    pattern = r"INSERT INTO gaze_events_viewer_(\d+) USING gaze_events TAGS\('session_end'\) VALUES \((NOW - \d+[mh]), '(viewer_\d+)', ([\d.]+), ([\d.]+), (\d+), ([\d.]+), (\d+), '(Male|Female)', '(\w+)', '(\w+)'\);"
    
    match = re.match(pattern, line.strip())
    if not match:
        return None
    
    viewer_num = match.group(1)
    time_expr = match.group(2)
    viewer_id = match.group(3)
    gaze_time = match.group(4)
    session_duration = match.group(5)
    gaze_count = match.group(6)
    engagement_rate = match.group(7)
    age = match.group(8)
    gender = match.group(9)
    emotion = match.group(10)
    ad_name = match.group(11)
    
    # Build JSON string using dict and simple string manipulation
    json_parts = []
    json_parts.append('"timestamp":"2026-02-09T00:00:00Z"')
    json_parts.append('"event":"session_end"')
    json_parts.append(f'"viewer_id":"{viewer_id}"')
    json_parts.append(f'"session_stats":{{"total_gaze_time":{gaze_time},"gaze_count":{gaze_count},"session_duration":{session_duration},"engagement_rate":{engagement_rate}}}')
    json_parts.append(f'"demographics":{{"age":{age},"gender":"{gender}","emotions":{{"{emotion}":{gaze_count}}}}}')
    json_parts.append(f'"ad_context":{{"ad_name":"{ad_name}"}}')
    
    json_data = '{' + ','.join(json_parts) + '}'
    
    # Build new INSERT statement
    new_insert = f"INSERT INTO gaze_events_session_end USING gaze_events TAGS('session_end', '{viewer_id}') VALUES ({time_expr}, '{json_data}');"
    
    return new_insert

def main():
    input_file = 'tdengine_init.sql'
    output_file = 'tdengine_init_converted.sql'
    
    with open(input_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    converted_lines = []
    converted_count = 0
    
    for line in lines:
        # Check if this is an INSERT line that needs conversion
        if line.strip().startswith('INSERT INTO gaze_events_viewer_'):
            converted = convert_insert_to_json(line)
            if converted:
                converted_lines.append(converted + '\n')
                converted_count += 1
            else:
                print(f"Warning: Could not convert line: {line[:80]}...")
                converted_lines.append(line)
        else:
            # Keep other lines as-is
            converted_lines.append(line)
    
    # Write converted file
    with open(output_file, 'w', encoding='utf-8') as f:
        f.writelines(converted_lines)
    
    print(f"Conversion complete!")
    print(f"Converted {converted_count} INSERT statements")
    print(f"Output written to: {output_file}")

if __name__ == '__main__':
    main()
