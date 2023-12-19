import requests
import torch
import cv2
import kits
from Service import Service

username = input('username: ')
password = input('password: ')

service = Service(username, password)

model = torch.hub.load('ultralytics/yolov5', 'yolov5s', _verbose=False)

capture = cv2.VideoCapture(0)
capture.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

watchMode = False
watchTargets = []
alreadyMissings = []

while True:
  _, frame = capture.read()
  cv2.imshow('KeepMyThings - Edge on Desktop', frame)
  service.appendFrame(frame)
  
  key = cv2.waitKey(50)

  if key == ord('q'): break

  found = list(model(frame).pandas().xyxy[0]['name'])

  if watchMode == False and key == ord('w'):
    watchTargets = found
    print(f'now watching... {found}')
  else:
    print(found)
    diff = kits.compareList(watchTargets, found)
    lost = [item for (item, count) in diff.items() if count > 0]
    newlyLostDiff = kits.compareList(lost, alreadyMissings)
    newlyLost = [item for (item, count) in newlyLostDiff.items() if count > 0]
    
    if len(newlyLost):
      print(f'{lost} are missing!')
      service.createHistory(lost)
      alreadyMissings = lost

capture.release()
cv2.destroyAllWindows()