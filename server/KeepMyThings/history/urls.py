from django.urls import path

from . import views

urlpatterns = [
    path('', view=views.historyCreation, name='endpoint for history creation'),
    path('/', view=views.historyList, name='endpoint for fetching history list'),
    path('<int:id>', view=views.historyRetrieval, name='endpoint for history retrieval'),
    path('<int:id>/delete', view=views.historyDeletion, name='endpoint for history deletion')
]