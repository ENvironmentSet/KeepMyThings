from django.urls import path

from . import views

urlpatterns = [
  path('', view=views.streams, name='list of available streams'),
  path('<streamID>', view=views.stream, name='endpoint for stream creation/retrieval/update'),
  path('<streamID>/delete', view=views.streamDeletion, name='endpoint for stream deletion')
]