#!/bin/bash

lxterminal -e bash -c "
cd /home/william/ai_digital_signage/microservices/audience-analysis-service
source ai_digital_signage_env/bin/activate
jupyter notebook
exec bash
"
