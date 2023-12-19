import torch
import cv2
from Service import Service
import simpleaudio

def compareList(xs, ys):
  diffMap = {}

  for x in xs:
    if x in diffMap:
      diffMap[x] += 1
    else:
      diffMap[x] = 1
  
  for y in ys:
    if y in diffMap:
      diffMap[y] -= 1
    else:
      diffMap[y] = -1
  
  return diffMap

class App:
  model = torch.hub.load('ultralytics/yolov5', 'yolov5s', _verbose=False)
  KEY_WAIT_TIME = 10
  ANALYSIS_GROUP_SIZE = 25
  analysisCalls = 0
  alarm = simpleaudio.WaveObject.from_wave_file('./siren.wav')

  def __init__(self, username, password):
    self.username = username
    self.password = password

  def __enter__(self):
    self.service = Service(self.username, self.password)

    self.capture = cv2.VideoCapture(0)
    self.capture.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    self.capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

    self.isKeepingThings = False
    self.watchTargets = []
    self.missingThings = []

    return self
  
  def __exit__(self, _1, _2, _3):
    self.capture.release()
    cv2.destroyAllWindows()
  
  def searchThings(self, frame):
    return list(self.model(frame).pandas().xyxy[0]['name'])

  def checkLosts(self, found):
    print(f'found {found}')

    diff = compareList(self.watchTargets, found)
    lost = [item for (item, count) in diff.items() if count > 0]
    newlyLostDiff = compareList(lost, self.missingThings)
    newlyLost = [item for (item, count) in newlyLostDiff.items() if count > 0]

    return (lost, newlyLost)
  
  def watch(self, things):
    print(f'now watching {things}')
    self.isKeepingThings = True
    self.watchTargets = things

  def stream(self, frame):
    self.service.appendFrame(frame)
  
  def display(self, frame):
    cv2.imshow('KeepMyThings - Edge on Desktop', frame)
  
  def fetchFrame(self):
    return self.capture.read()[1]
  
  def input(_):
    return cv2.waitKey(App.KEY_WAIT_TIME)
  
  def run(self):
    while True:
      frame = self.fetchFrame()
      self.display(frame)
      self.stream(frame)

      key = self.input()
      if key == ord('q'): break
      elif key == ord('w') and not self.isKeepingThings: self.watch(self.searchThings(frame))
      else:
        self.analysisCalls += 1
        if self.analysisCalls != self.ANALYSIS_GROUP_SIZE: continue

        self.analysisCalls = 0
        lost, newlyLost = self.checkLosts(self.searchThings(frame))

        if len(newlyLost):
          print(f'{newlyLost} are missing')
          simpleaudio.stop_all()
          self.alarm.play()
          self.service.createHistory(lost)
          self.missingThings = newlyLost

if __name__ == '__main__':
  username = input('username: ')
  password = input('password: ')

  with App(username, password) as app:
    app.run()