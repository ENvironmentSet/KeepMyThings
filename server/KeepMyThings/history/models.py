from django.contrib.auth.models import User
from django.db import models
import json

def thumbnailUploadPath(instance, _):
  return f'{instance.user.id}/thumbnails/{instance.date.timestamp()}.jpeg'

def footageUploadPath(instance, _):
  return f'{instance.user.id}/footages/{instance.date.timestamp()}.mp4'

class History(models.Model):
  user = models.ForeignKey(User, on_delete=models.CASCADE)
  date = models.DateTimeField('Date and time when user\'s belongings are lost')
  thumbnail = models.ImageField(upload_to=thumbnailUploadPath)
  footage = models.FileField(upload_to=footageUploadPath)
  lost = models.JSONField()

  def export(self):
    return { 
      'date': self.date.strftime("%Y-%m-%dT%H:%M:%S"),
      'thumbnail': self.thumbnail.url,
      'footage': self.footage.url,
      'lost': self.lost
    }
  
  def __str__(self):
    return f'user({self.user.id}), {json.JSONEncoder().encode(self.lost)} are missing'