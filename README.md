# place-search-service-api

`장소 검색 서비스`를 위한 서버 어플리케이션을 구현

* [Feature](#feature)
* [Getting started](#getting-started)
* [Run test](#run-test)
* [System architecture](#system-architecture)
* [List of open source libraries applied to the application and their purpose](#list-of-open-source-libraries-applied-to-the-application-and-their-purpose)
* [Implemented technical requirements](#implemented-technical-requirements)
* [Code format](*)

## Feature

[1] 장소 검색 API
* 장소명, 지번 주소, 문자열 유사도 비교 적용

[2] 검색 키워드 목록 조회 API
* redis 의 sorted set data type 을 이용하여, 검색 키워드와 score 를 저장 및 불러올 수 있도록 구성

## Getting started 

아래 두가지 항목에 대해 사전 설치가 되어 있어야 합니다 :

* amazon corretto jdk 11 (https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
* docker (https://www.docker.com/products/docker-desktop/)

검색 키워드를 저장하기 위해 redis in-memory db 준비 :
    
    $ docker pull redis 
    $ docker network create redis-net 
    $ docker run --name redis -p 6379:6379 --network redis-net -d redis redis-server --appendonly yes

어플리케이션 실행

	$ cd ~/{프로젝트 위치}
	$ ./gradle bootRun -Dhealthcheck.filepath=alive.html -Dio.netty.leakDetection.level=advanced -Djava.net.preferIPv4Stack=true -Dreactor.netty.http.server.accessLogEnabled=false -Dexternal.api.dryRun=false

로그 위치

	$ cd ~/{프로젝트 위치}/logs/access.log - NDJSON 형식의 request/response 로그
	$ cd ~/{프로젝트 위치}/logs/business.log - NDJSON 형식의 application 로그, External API 관련 로그

어플리케이션 동작 여부

    curl --location --request GET 'http://localhost:8080/health-check

## Run test
* 로직 테스트
* API 테스트 


    curl --location --request GET 'http://localhost:8080/v1/place?query=%EA%B3%B1%EC%B0%BD'
    curl --location --request GET 'http://localhost:8080/v1/place/search-keywords'

## How it works
* 어플리케이션은 Spring Boot(Webflux, Lettuce)를 사용합니다.
* Webflux 에서 지원하는 Functional routing and handling 모델 개념을 적용 하였습니다. 

그리고 주요 코드는 다음과 같이 구성됩니다.

1. `route`는 api endpoint 를 정의 합니다.
2. `handler`는 entity 와 서비스를 포함한 비즈니스 로직의 정의 합니다.
3. `dto`은 데이터 전송 개체를 쿼리하기 위한 객체들의 정의 합니다.
4. `repository`에는 database 의 실제 crud 정의 합니다.
5. `application.yml` 에는 각종 정적 설정 정보를 정의 합니다.

## System architecture
![](architecture.jpg)
* AWS 기반에서 구성 되며, 실제 서비스를 구성 하게 된다는 가정하에 구상하였습니다.
* 고가용성 확보
  * MultiAZ 를 A Zone 과 C Zone 으로 각각 나누어,
    * 어플리케이션 서버 영역의 공격자나 외부 접근을 막기위해 private subnet 으로 구성하고
    * 마찬가지로 Database 도 공격자나 외부 접근을 막기위해 private subnet 을 구성 합니다.
    * inbound 의 경우 Application Load Balancer 를 통해 Target group 로 구성된 어플리케이션 서버에 round robin 방식으로 부하를 분산 시킵니다.
    * outbound 의 경우 NAT Gateway 를 통해 양 Zone 간 하나씩 public ip로 배치 하여, 서버가 늘어나더라도 목적지에서 추가적인 방화벽 설정 작업을 줄 일 수 있도록 구성 하였습니다.
    * Auto Scaling 의 경우 부하테스트와 실제 운영 지표를 cloud watch 를 통해 예상 요청 트래픽을 수용할 수 있는 적정 서버 수를 산정 하고, 수용 범위를 초과 하였을때는 양 Zone 에 n 개씩 늘리 수 있게 설정이 필요합니다.
  * 어플리케이션 내에 서버 캐싱을 적용 하여, write 보다 read 가 많은 요청의 경우나 데이터가 자주 바뀌지 않는 설정 정보의 경우 Database 에 접근 회수를 줄 일 수 있도록 설정함.
  * latency 를 줄이기 위해 리전간 시스템 구성도 고려 하였지만, 해당 과제에서 요구하는 범위 밖인 것으로 판단 하여 고려 하지 않았습니다.
* 내결함성 확보
  * 어플케이션내에 Circuit Breaker 기능을 추가하여, 필요 구간에서 지연이나 바로 복구가 불가능 한 상황 발생시, 문제가 되는 요청은 거부하여 요청이 밀려 요청지와 목적지 모두 서비스 불능 상태를 방지 시키고,
  * 다른 요청에 대해서는 정상 서비스 될 수 있도록 설정함.
  * 어플리케이션 내에서 Database 및 외부 시스템에 API 요청시 backoff 와 jitter 를 기본 적용 하여, failover 나 네트워크 순단 같은 상황시를 대비 할 수 있도록 설정함.
* CI/CD
  * 별도의 VPC 에 배포 환경 구축
  * alive.html
* Log & Monitoring
  * cloudwatch, cloudwatch log, insight, athena
* Database
  * Database는 낮은 지연율과 reactive programing 을 위해 
    * `Elasticache` (https://aws.amazon.com/ko/elasticache/) 
    * `Memory DB for Redis` (https://aws.amazon.com/ko/memorydb/) 또는 `Dynamo DB` (https://aws.amazon.com/ko/dynamodb/)
  * 를 선정 하였습니다.
  * 요구 사항에 좀 더 정교한 Transaction 이 필요하다면, 아래의 조합을 도입 할 수도 있을 것 같습니다.
    * `Aurora DB for mysql` (https://aws.amazon.com/ko/rds/aurora/)
    * `R2DBC` (https://r2dbc.io/)  

  
## List of open source libraries applied to the application and their purpose
| 라이브러리 | 용도 |
| -------- | -------- |
| org.springframework.boot:spring-boot-starter-webflux | |
| org.springframework.boot:spring-boot-starter-aop | |
| org.springframework.boot:spring-boot-configuration-processor | |
| org.springframework.boot:spring-boot-starter-data-redis-reactive | |
| io.projectreactor.addons:reactor-extra | |
| io.projectreactor:reactor-tools | |
| io.projectreactor:reactor-test | |
| io.github.resilience4j:resilience4j-spring-boot2 | |
| io.github.resilience4j:resilience4j-reactor | |
| org.springframework.boot:spring-boot-starter-logging | |
| net.logstash.logback:logstash-logback-encoder | |
| org.springframework.boot:spring-boot-starter-cache | |
| com.github.ben-manes.caffeine:caffeine | |
| info.debatty:java-string-similarity | |
| org.modelmapper:modelmapper | |
| org.projectlombok:lombok | |
| io.projectreactor.tools:blockhound | |
| org.springframework.boot:spring-boot-starter-test | |
| org.junit.jupiter:junit-jupiter-api | |
| org.junit.jupiter:junit-jupiter-engine | |
| io.netty:netty-resolver-dns-native-macos:4.1.86.Final:osx-aarch_64 | |
| org.springframework.cloud:spring-cloud-dependencies:2021.0.2 | |

## Implemented technical requirements
* 동시성 이슈가 발생할 수 있는 부분을 염두에 둔 설계 및 구현 (예시. 키워드 별로 검색된 횟수)
  * 키워드 별로 검색된 횟수의 경우 일반 RDB의 경우 DeadLock 이 발생 할 수 있어,
  * 정렬 기능
* 카카오, 네이버 등 검색 API 제공자의 “다양한” 장애 발생 상황에 대한 고려
* 구글 장소 검색 등 새로운 검색 API 제공자의 추가 시 변경 영역 최소화에 대한 고려
* 서비스 오류 및 장애 처리 방법에 대한 고려
* 대용량 트래픽 처리를 위한 반응성(Low Latency), 확장성(Scalability), 가용성(Availability)을 높이기 위한 고려
* 지속적 유지 보수 및 확장에 용이한 아키텍처에 대한 설계
* 이 외에도 본인의 기술 역량을 잘 드러낼 수 있는 부분을 과제 코드 내에서 강조

## Code format 
* 코드 형식에는 spotless를 사용하였습니다. 

    
    ./gradlew spotlessJavaApply

