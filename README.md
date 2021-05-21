# 실습을 위한 개발 환경 세팅
* https://github.com/slipp/web-application-server 프로젝트를 자신의 계정으로 Fork한다. Github 우측 상단의 Fork 버튼을 클릭하면 자신의 계정으로 Fork된다.
* Fork한 프로젝트를 eclipse 또는 터미널에서 clone 한다.
* Fork한 프로젝트를 eclipse로 import한 후에 Maven 빌드 도구를 활용해 eclipse 프로젝트로 변환한다.(mvn eclipse:clean eclipse:eclipse)
* 빌드가 성공하면 반드시 refresh(fn + f5)를 실행해야 한다.

# 웹 서버 시작 및 테스트
* webserver.WebServer 는 사용자의 요청을 받아 RequestHandler에 작업을 위임하는 클래스이다.
* 사용자 요청에 대한 모든 처리는 RequestHandler 클래스의 run() 메서드가 담당한다.
* WebServer를 실행한 후 브라우저에서 http://localhost:8080으로 접속해 "Hello World" 메시지가 출력되는지 확인한다.

# 각 요구사항별 학습 내용 정리
* 구현 단계에서는 각 요구사항을 구현하는데 집중한다. 
* 구현을 완료한 후 구현 과정에서 새롭게 알게된 내용, 궁금한 내용을 기록한다.
* 각 요구사항을 구현하는 것이 중요한 것이 아니라 구현 과정을 통해 학습한 내용을 인식하는 것이 배움에 중요하다. 

### 요구사항 1 - http://localhost:8080/index.html로 접속시 응답
1. **ServerSocket**  
   서버 프로그램을 구현할 때 사용. 일반적인 서버 프로그램의 과정은 아래의 6단계를 거침.
    1. 서버 소켓 생성, 포트 바인딩
    2. 클라이언트로부터의 연결을 기다리고(Connect Listen) 요청이 오면 수락
    3. 클라이언트 소켓에서 가져온 InputStream 을 읽음
    4. 응답이 있다면 OutputStream을 통해 클라이언트에 데이터를 보냄s
    5. 클라이언트와의 연결을 닫음
    6. 서버 종료
    
2. **Content-Type vs Accept**
    - Content-type
        - 리소스의 미디어 타입(MIME 타입)을 나타냄
        - HTTP 메시지(요청 & 응답)에 담겨 보내는 데이터 형식을 알려주는 헤더
        - 응답 내에 있는 Content-Type 헤더는 클라이언트에게 반환된 컨텐츠의 컨텐츠 유형이 실제로 무엇인지를 알려줌
        - POST, PUT 요청에서 클라이언트는 서버에게 어떤 유형의 데이터가 실제로 전송됐는지를 알려줌
            - GET의 경우에는 Content-Type 헤더가 불필요. URI와 쿼리 파라미터로도 충분하기 때문.
            - POST/PUT의 경우에는 데이터 형식이 xml, json 등 다양한 형태로 전달될 수 있기 때문에 필요.
    - Accept
        - MIME 타입으로 표현되는, 클라이언트가 이해 가능한 컨텐츠 타입이 무엇인지를 알려줌
        - 서버는 제안 중 하나를 선택하고 사용해 `Content-Type` 응답 헤더로 클라이언트에게 선택된 타입을 알려줌
    
### 요구사항 2 - get 방식으로 회원가입
* 

### 요구사항 3 - post 방식으로 회원가입
* 

### 요구사항 4 - redirect 방식으로 이동
* 

### 요구사항 5 - cookie
* 

### 요구사항 6 - stylesheet 적용
* 

### heroku 서버에 배포 후
* 