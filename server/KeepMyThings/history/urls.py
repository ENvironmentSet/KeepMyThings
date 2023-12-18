from django.urls import path

from . import views

urlpatterns = [
    path('', view=views.history, name='endpoint for history creation and list fetching'),
    path('<int:id>', view=views.historyRetrieval, name='endpoint for history retrieval'),
    path('<int:id>/delete', view=views.historyDeletion, name='endpoint for history deletion')
]