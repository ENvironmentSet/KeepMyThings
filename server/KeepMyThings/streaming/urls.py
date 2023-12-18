from django.urls import path

from . import views

urlpatterns = [
  path('', view=views.streams, name='list of available streams'),
  path('<int:id>', view=views.stream, name='endpoint for stream creation/retrieval/update'),
  path('<int:id>/delete', view=views.streamDeletion, name='endpoint for stream deletion')
]