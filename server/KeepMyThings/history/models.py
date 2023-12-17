from django.contrib.auth.models import User
from django.db import models

def thumbnailUploadPath(instance, _):
  return f'{instance.user.id}/thumbnails/{instance.date.timestamp}'

def footageUploadPath(instance, _):
  return f'{instance.user.id}/footages/{instance.date.timestamp}'

class History(models.Model):
  user = models.ForeignKey(User, on_delete=models.CASCADE)
  date = models.DateTimeField('Date and time when user\'s belongings are lost')
  thumbnail = models.ImageField(upload_to=thumbnailUploadPath)
  footage = models.FileField(upload_to=footageUploadPath)
  lost = models.JSONField()

  def export(self):
    return { 
      'date': str(self.date.timestamp()),
      'thumbnail': self.thumbnail.url,
      'footage': self.footage,
      'lost': self.lost
    }