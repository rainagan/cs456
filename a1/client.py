#!/usr/bin/env python3
import sys, socket, os, math, io, time
from struct import pack

def checkInt(s):
    try:
        int(s)
        return True
    except ValueError:
        return False

# 1. terminate server: client <host> <port> F
# 2. download: client <host> <port> G<key> <file name> <recv size>
# 3. upload: client <host> <port> P<key> <file name> <send size> <wait time>

if len(sys.argv) == 4:
    HOST, PORT, command = sys.argv[1:4]
    if command == "F" :
        PORT = int(PORT)
        #creat tcp socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
        sock.send(str.encode(command))
    else:
        print("Please use correct command. etc ./client <host> <port> F to terminate server")



#download
elif len(sys.argv) == 6:
    HOST, PORT, command, filename, recvsize = sys.argv[1:6]
    if command[0] == "G":
        recvsize = int(recvsize)
        PORT = int(PORT)
        sendCommad = pack("9s", str.encode(command))
         #creat tcp socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
        sock.send(sendCommad)
        file = open(filename,"wb+")
        while True:
            received = sock.recv(recvsize)
            if not received: break
            file.write(received)
        file.close()
    else:
        print("Please use correct command. etc ./client <host> <port> G<key> <file name> <recv size> to download")




#upload
elif len(sys.argv) == 7:
    HOST, PORT, command, filename, sendsize, waittime = sys.argv[1:7]
    if command[0] == "P":
        waittime = int(waittime)
        sendsize = int(sendsize)
        PORT = int(PORT)
        sendCommad = pack("9s", str.encode(command))

        if(checkInt(filename)):
            fileSize = int(filename)
            file = io.BytesIO(bytes(fileSize))
        else:
            fileSize = os.path.getsize(filename)
            file = open(filename, 'rb')

        #creat tcp socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((HOST, PORT))
        #sned key
        sock.send(sendCommad)
        while True:
            block = file.read(sendsize)
            if not block: break
            sock.send(block)
            time.sleep(waittime / 1000.0)
    else:
        print("Please use correct command. etc ./client <host> <port> P<key> <file name> <recv size> to upload")

else:
    print("Please use correct command.")


# except:
#     print("Please use correct command.")
#     exit()
