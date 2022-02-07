import threading
import socket
import time

SEND_THREAD_NUM = 2

class SendThread(threading.Thread):
    def __init__(self, address):
        threading.Thread.__init__(self)
        self.address = address

    def run(self):
        start_time = time.time()
        while time.time() - start_time < 15:
            sock.sendto(bytes(raw_data, encoding='utf8'),  self.address)


size = 1024
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(('', 9876))

raw_data = ''
for i in range(size):
    raw_data = raw_data + '0'

try:
    while True:
        data, address = sock.recvfrom(size)
        print('receive package, data=%s, address=%s'%(str(data), str(address)))
        send_threads = []
        for i in range(SEND_THREAD_NUM):
            send_threads.append(SendThread(address))
        
        for thread in send_threads:
            thread.start()

finally:
    sock.close()
