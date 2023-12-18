import requests
import torch
import cv2

username = input('username: ') #test
password = input('password: ') #test

sessionid = requests.post('http://127.0.0.1:8000/auth/login', data={ 'username': username, 'password': password }).cookies['sessionid']

model = torch.hub.load('ultralytics/yolov5', 'yolov5s', _verbose=False)

capture = cv2.VideoCapture(0)
capture.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

while cv2.waitKey(50) != ord('q'):
    ret, frame = capture.read()

    cv2.imshow('KeepMyThings - Edge on Desktop', frame)

    found = list(model(frame).pandas().xyxy[0]['name'])

capture.release()
cv2.destroyAllWindows()