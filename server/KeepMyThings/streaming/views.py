from django.views.decorators.http import require_POST, require_safe
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone
from django.core.exceptions import ObjectDoesNotExist

from .models import Stream

@csrf_exempt
@require_safe
def streams(request):
  if not request.user.is_authenticated: return HttpResponse(status=401)

  streams = Stream.objects.filter(user=request.user)

  return JsonResponse([stream.export() for stream in streams.iterator()], safe=False)

@csrf_exempt
def stream(request, streamID):
  if not request.user.is_authenticated: return HttpResponse(status=401)

  if request.method == 'GET': #get_object_or_404
    try:
      stream = Stream.objects.get(streamID=streamID)

      if stream.user.id != request.user.id: return HttpResponse(status=403)

      return JsonResponse(stream.export())
    except ObjectDoesNotExist:
      return HttpResponse(status=404)
  elif request.method == 'POST':
    try:
      stream = Stream.objects.get(streamID=streamID)

      if stream.user.id != request.user.id: return HttpResponse(status=403)
      
      stream.video = request.FILES['video']
      stream.croppedTime = timezone.now()
      stream.save()

      return HttpResponse()
    except ObjectDoesNotExist:
      Stream.objects.create(user=request.user, video=request.FILES['video'], croppedTime=timezone.now())

      return HttpResponse()
  else:
    return HttpResponse(status=405)
  
@csrf_exempt
@require_POST
def streamDeletion(request, streamID):
  if not request.user.is_authenticated: return HttpResponse(status=401)

  try:
    stream = Stream.objects.get(streamID=streamID)

    if stream.user.id != request.user.id: return HttpResponse(status=403)

    stream.delete()

    return HttpResponse()
  except ObjectDoesNotExist:
    return HttpResponse(status=404)