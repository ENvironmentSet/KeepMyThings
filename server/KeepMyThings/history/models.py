from django.contrib.auth.models import User
from django.db import models

class History(models.Model):
  user = models.ForeignKey(User)
  date = models.DateTimeField('Date and time when user\'s belongings are lost')
  thumbnail = models.ImageField(upload_to=lambda instance, _: f'{instance.user.id}/thumbnails/{str(instance.date.timestamp)}')
  footage = models.FileField(upload_to=lambda instance, _: f'{instance.user.id}/footages/{str(instance.date.timestamp)}')
  lost = models.JSONField()

  def export(self):
    return { 
      'date': str(self.date.timestamp()),
      'thumbnail': self.thumbnail.url,
      'footage': self.footage,
      'lost': self.lost
    }