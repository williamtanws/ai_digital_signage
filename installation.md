Python and picamera2 Libraries Installation
===========================================
sudo apt update
sudo apt install -y libcamera-dev python3-pyqt5 libatlas-base-dev
sudo apt-get install libcap-dev
pip install --upgrade pip setuptools
pip install picamera2

Project Installation
===========================================
python3 -m venv ai_digital_signage_env --system-site-packages
source ai_digital_signage_env/bin/activate
pip install -r requirements.txt

// Check Virtual Environment is Available in Jupyter
python -m ipykernel install --user --name=ai_digital_signage_env --display-name "Python (ai_digital_signage_env)"
pip install bme680

Running Jupyter
===========================================
source ai_digital_signage_env/bin/activate
jupyter notebook
