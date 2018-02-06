#!/usr/bin/env python3

import socketserver, socket, threading


upload = {}
download = {}
threadList = []
terminate = False

def shutdownServer():
    global server
    server.shutdown()

def handlethread(socketup, socketdown):
    data = socketup.recv(512)
    while data:
        socketdown.send(data)
        data = socketup.recv(512)
    socketdown.close()


class ThreadedTCPRequestHandler(socketserver.BaseRequestHandler):




    def handle(self):
        global download, upload, terminate, threadList, cv, cvHandle, handlethread, shutdownServer
        # self.request is the TCP socket connected to the client

        command = self.request.recv(1)
        if(command.decode("utf-8") == "F"):
            terminate = True
            for i in range(len(threadList)):
                threadList[i].join()
            with cvHandle:
                cvHandle.notify_all()
            thread = threading.Thread(target = shutdownServer)
            thread.start()
            thread.join()
            with cv:
                cv.notify_all()
            return
            # thread.join()
        #upload
        elif command.decode("utf-8") == "P" and not terminate:
            key = (self.request.recv(8)).decode('utf-8')
            if key not in upload:
                upload[key] = [self.request]
            else:
                upload[key].append(self.request)
            if key not in download:
                with cv:
                    while key not in download:
                        cv.wait()
                        if terminate:
                            return
                    socketup = upload[key].pop()
                    socketdown = download[key].pop()
                    if len(upload[key]) == 0:
                        del upload[key]
                    if len(download[key]) == 0:
                        del download[key]
                thread = threading.Thread(target = handlethread, args = (socketup, socketdown, ))
                threadList.append(thread)
                thread.start()
            else:
                with cv:
                    cv.notify_all()


        #download
        elif command.decode("utf-8") == "G" and not terminate:
            key = (self.request.recv(8)).decode('utf-8')
            if key not in download:
                download[key] = [self.request]
            else:
                download[key].append(self.request)
            if key not in upload:
                with cv:
                    while key not in upload:
                        cv.wait()
                        if terminate:
                            return
                    socketup = upload[key].pop()
                    socketdown = download[key].pop()
                    if len(upload[key]) == 0:
                        del upload[key]
                    if len(download[key]) == 0:
                        del download[key]
                thread = threading.Thread(target = handlethread, args = (socketup, socketdown, ))
                threadList.append(thread)
                thread.start()
            else:
                with cv:
                    cv.notify_all()

        cvHandle.acquire()
        cvHandle.wait()


class ThreadedTCPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    pass

HOST, PORT = "", 0
# Create the server, binding to localhost on port 9999
cv = threading.Condition()
cvHandle = threading.Condition()
server = ThreadedTCPServer((HOST, PORT), ThreadedTCPRequestHandler)
print(server.server_address[1])
# Activate the server; this will keep running until you
# interrupt the program with Ctrl-C
server.serve_forever()