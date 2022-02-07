#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>
#include <time.h>
#include <sys/time.h>

#define PORT 9876
#define BUFFER_SIZE 512


int main()
{
    int sockfd;
    struct sockaddr_in serv_addr;
    struct sockaddr_in cli_addr;
    int addlen = sizeof(struct sockaddr);
    char buf[BUFFER_SIZE];
    int ret;


    if((sockfd = socket(AF_INET,SOCK_DGRAM,0)) < 0)
    {
        printf("socket error!\n");
    }
    bzero(&serv_addr,addlen);
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(PORT);
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    size_t buf_size = BUFFER_SIZE * 2;
    setsockopt(sockfd, SOL_SOCKET, SO_SNDBUF,  &buf_size, (socklen_t)sizeof(buf_size));


    if((ret = bind(sockfd,(struct sockaddr*)&serv_addr,addlen)) < 0)
    {
        printf("bind error!\n");
    }


    while(1)
    {
        if(recvfrom(sockfd,buf,100,0,(struct sockaddr*)&cli_addr,&addlen) < 0)
        {
            fprintf(stderr,"error is %s\n",strerror(errno));
        }
        printf("read is %s\n",buf);

        memset(buf,0,100);


        //char text[] = "000";
        memset(buf,'0', BUFFER_SIZE);
        //memcpy(buf, text, sizeof(text));
        struct timeval tv,tv2;
        gettimeofday(&tv,NULL);
        long current_timestamp_ms = tv.tv_sec * 1000 + tv.tv_usec / 1000;

        while(1) {
            if(sendto(sockfd,buf,BUFFER_SIZE,0,(struct sockaddr*)&cli_addr,addlen) < 0)
            {
                printf("send error!\n");
            }
            gettimeofday(&tv2,NULL);
            long tmp_timestamp_ms = tv2.tv_sec * 1000 + tv2.tv_usec / 1000;
            if(tmp_timestamp_ms - current_timestamp_ms > 15000) break;
            //printf("start_time = %ld, cur_time=%ld \n",current_timestamp_ms, tmp_timestamp_ms);
        }
        printf("Sending Finish!\n");
    }
   close(sockfd);

}
