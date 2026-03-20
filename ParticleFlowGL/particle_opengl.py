import pygame, random, math, sys
pygame.init()
screen = pygame.display.set_mode((0,0), pygame.FULLSCREEN)
w, h = screen.get_size()
pygame.display.set_caption('ParticleFlowGL - Touch Demo')
clock = pygame.time.Clock()

NUM_PARTICLES = 8192
particles = [[random.uniform(0,w), random.uniform(0,h), 0,0] for _ in range(NUM_PARTICLES)]
touches = [w/2, h/2]

while True:
    for event in pygame.event.get():
        if event.type == pygame.QUIT or (event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE):
            pygame.quit(); sys.exit()
        if event.type == pygame.MOUSEMOTION:
            touches = list(event.pos)
    
    screen.fill((10,10,20))
    for i in range(NUM_PARTICLES):
        x,y,dx,dy = particles[i]
        diffx, diffy = touches[0]-x, touches[1]-y
        distsq = (diffx*diffx + diffy*diffy + 10)
        dx += 50000 * diffx / distsq
        dy += 50000 * diffy / distsq
        x += dx; y += dy
        dx *= 0.96; dy *= 0.96
        if x < 0: x = w; if x > w: x = 0
        if y < 0: y = h; if y > h: y = 0
        speed = math.log(dx*dx + dy*dy + 1) / 4.5
        r,g,b = int(speed*2*255), int(speed*255), int(speed/2*255)
        particles[i] = [x,y,dx,dy]
        pygame.draw.circle(screen, (min(r,255), min(g,255), min(b,255)), (int(x),int(y)), 2)
    
    pygame.display.flip()
    clock.tick(60)
