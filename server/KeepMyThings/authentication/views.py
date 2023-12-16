from django.views.decorators.http import require_POST
from django.contrib.auth import authenticate, login as attachSession, logout as clearSession
from django.http import HttpResponse
from django.contrib.auth.models import User
from django.views.decorators.csrf import csrf_exempt

@csrf_exempt
@require_POST
def signin(request):
  username = request.POST['username']
  password = request.POST['password']

  if authenticate(username=username, password=password) is None:
    user = User.objects.create_user(username=username, password=password)
    attachSession(request, user)

    return HttpResponse()
  else:
    return HttpResponse(status=409)

@csrf_exempt
@require_POST
def signout(request):
  if request.user.is_authenticated:
    clearSession(request)
    request.user.delete()

    return HttpResponse()
  else:
    return HttpResponse(status=400)

@csrf_exempt
@require_POST
def login(request):
  username = request.POST['username']
  password = request.POST['password']

  user = authenticate(username=username, password=password)
  if user is not None:
    attachSession(request, user)

    return HttpResponse()
  else:
    return HttpResponse(status=401)

@csrf_exempt
@require_POST
def logout(request):
  if request.user.is_authenticated:
    clearSession(request)

    return HttpResponse()
  else:
    return HttpResponse(status=400)