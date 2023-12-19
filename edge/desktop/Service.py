import requests
import cv2
import json
import time

class LoginFailedError(Exception):
  def __init__(self, username, password):
    self.username = username
    self.password = password
  
  def __str__(self):
    return f'Failed to login, tried with username: \'{self.username}\' and password: \'{self.password}\''

class Service:
  SERVER = '127.0.0.1:8000'
  CHUNKS_MEMORY_LIMIT = 3
  CHUNK_FPS = 20
  CHUNK_CODEC = cv2.VideoWriter_fourcc(*'avc1')
  CHUNK_SIZE = (640, 480)
  CHUNK_LENGTH = 5

  def __init__(self, username, password):
    loginResult = requests.post(f'http://{Service.SERVER}/auth/login', data={ 'username': username, 'password': password })

    if loginResult.status_code != 200: raise LoginFailedError(username, password)
    else: self.sessionid = loginResult.cookies['sessionid']

    self.streamID = -1

    self.videoChunks = []
    self.nextVideoChunkFrames = []
  
  def createHistory(self, lost):
    cv2.imwrite('thumbnail.jpeg', self.nextVideoChunkFrames[-1]) # replace this step with imencode
    thumbnail = open('thumbnail.jpeg', 'rb')
    footageChunks = (self.videoChunks[-1] if len(self.videoChunks) > 0 else []) + self.nextVideoChunkFrames
    chunkWriter = cv2.VideoWriter('chunk.mp4', Service.CHUNK_CODEC, Service.CHUNK_FPS, Service.CHUNK_SIZE)
    for chunkFrame in footageChunks:
      chunkWriter.write(chunkFrame)
    chunkWriter.release()
    footage = open('chunk.mp4', 'rb')

    requests.post(
      url=f'http://{Service.SERVER}/history/', 
      cookies={ 'sessionid': self.sessionid }, 
      data={ 'lost': json.dumps(lost) },
      files={ 'thumbnail': thumbnail, 'footage': footage }
    )
  
  def appendFrame(self, frame):
    if len(self.nextVideoChunkFrames) < Service.CHUNK_FPS * Service.CHUNK_LENGTH:
      self.nextVideoChunkFrames.append(frame)
    else:
      chunkWriter = cv2.VideoWriter('chunk.mp4', Service.CHUNK_CODEC, Service.CHUNK_FPS, Service.CHUNK_SIZE)
      for chunkFrame in self.nextVideoChunkFrames:
        chunkWriter.write(chunkFrame)
      chunkWriter.release()
      chunk = open('chunk.mp4', 'rb')
      if self.streamID == -1:
        streams = requests.get(f'http://{Service.SERVER}/stream/', cookies={ 'sessionid': self.sessionid }).json()
        self.streamID = len(streams)
      requests.post(f'http://{Service.SERVER}/stream/{self.streamID}', cookies={ 'sessionid': self.sessionid }, files={ 'video': chunk })
      self.videoChunks.append(self.nextVideoChunkFrames)
      self.nextVideoChunkFrames = [frame]

      if len(self.videoChunks) > Service.CHUNKS_MEMORY_LIMIT:
        self.videoChunks = self.videoChunks[1:]