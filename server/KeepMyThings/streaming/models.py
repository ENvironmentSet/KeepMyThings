from django.db import models
from django.contrib.auth.models import User
import uuid

def videoPath(instance, _):
  return f'{instance.user.id}/streams/{instance.fileID}.mp4'

class Stream(models.Model):
  user = models.ForeignKey(User, on_delete=models.CASCADE)
  fileID = models.UUIDField(default=uuid.uuid4)
  video = models.FileField(upload_to=videoPath)
  croppedTime = models.DateTimeField('Date and time when video updated')

  def export(self):
    return {
      'video': self.video.url,
      'croppedTime': str(self.croppedTime.timestamp())
    }
  
  def __str__(self):
    return f'user({self.user.id}), stream {self.id}'