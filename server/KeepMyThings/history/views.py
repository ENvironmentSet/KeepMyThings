from django.views.decorators.http import require_POST, require_safe
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone
from django.core.exceptions import ObjectDoesNotExist
from .models import History
import json

@csrf_exempt
def history(request):
  if not request.user.is_authenticated: return HttpResponse(status=401)

  if request.method == 'GET':
    return JsonResponse([ history.export() for history in History.objects.filter(user=request.user).order_by('-date').iterator() ], safe=False) 
  elif request.method == 'POST':
    thumbnail = request.FILES['thumbnail']
    footage = request.FILES['footage']
    lost = json.JSONDecoder().decode(request.POST['lost'])

    History.objects.create(user=request.user, date=timezone.now(), thumbnail=thumbnail, footage=footage, lost=lost)
      
    return HttpResponse()
  else:
    return HttpResponse(status=405)

@csrf_exempt
@require_safe
def historyRetrieval(request, id):
  if not request.user.is_authenticated: return HttpResponse(status=401)
  
  try:
    history = History.objects.get(id=id)

    if history.user.id != request.user.id: return HttpResponse(status=403)

    return JsonResponse(history.export())
  except ObjectDoesNotExist:
    return HttpResponse(status=404)

@csrf_exempt
@require_POST
def historyDeletion(request, id):
  if not request.user.is_authenticated: return HttpResponse(status=401)

  try:
    history = History.objects.get(id=id)

    if history.user.id != request.user.id: return HttpResponse(status=403)

    history.delete()

    return HttpResponse()
  except ObjectDoesNotExist:
    return HttpResponse(status=404)