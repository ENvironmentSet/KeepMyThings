from django.conf import settings
from django.core.files.storage import FileSystemStorage
import os
from django.db import models
from django.contrib.auth.models import User
import uuid

class OverwriteStorage(FileSystemStorage):
    def get_available_name(self, name, max_length=None):
        if self.exists(name):
            os.remove(os.path.join(settings.MEDIA_ROOT, name))
        return name

def videoPath(instance, _):
  return f'{instance.user.id}/streams/{instance.streamID}.mp4'

class Stream(models.Model):
  user = models.ForeignKey(User, on_delete=models.CASCADE)
  streamID = models.UUIDField(default=uuid.uuid4)
  video = models.FileField(upload_to=videoPath, storage=OverwriteStorage())
  croppedTime = models.DateTimeField('Date and time when video updated')

  def export(self):
    return {
      'streamID': self.streamID,
      'video': self.video.url,
      'croppedTime': str(self.croppedTime.timestamp())
    }
  
  def __str__(self):
    return f'user({self.user.id}), stream {self.streamID}'