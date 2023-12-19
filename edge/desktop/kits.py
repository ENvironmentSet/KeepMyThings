def compareList(xs, ys):
  diffMap = {}

  for x in xs:
    if x in diffMap:
      diffMap[x] += 1
    else:
      diffMap[x] = 1
  
  for y in ys:
    if y in diffMap:
      diffMap[y] -= 1
    else:
      diffMap[y] = -1
  
  return diffMap