from django.urls import path

from . import views

urlpatterns = [
    path('signin', views.signin, name='signin'),
    path('signout', views.signout, name='signout'),
    path('login', views.login, name='login'),
    path('logout', views.logout, name='logout')
]