# 주택 금융 서비스
## 개발 프레임워크
Spring Boot 2.2.4

## 빌드 방법
### Repository를 Clone하거나 ZIP파일로 다운 받고 압축 푼 후 해당 폴더에서 다음 명령을 실행합니다.
```
./mvnw package
```
## 실행 방법
### 1. mvnw로 바로 실행
```
./mvnw spring-boot:run

# 기본으로 8080포트를 사용합니다. 포트를 변경하여 실행하고자 한다면 다음 명령을 실행합니다.
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=10050
```

### 2. jar 파일 실행 (**빌드한 이후여야 합니다**)
```
java -jar ./target/housing-finance-1.0.0.jar

# 기본으로 8080포트를 사용합니다. 포트를 변경하여 실행하고자 한다면 다음 명령을 실행합니다.
java -jar ./target/housing-finance-1.0.0.jar --server.port=10050
```
## 데이터베이스
### H2 Memory DB
1. institutes(institute_code, institute_name)  
기관 정보를 가지고 있는 테이블  

2. institute_supports(year, month, institute_code, support_amount)  
어떤 기관이 어떤 연도, 월에 얼마 만큼의 지원을 했는지에 대한 정보를 가지고 있는 테이블  

## 문제해결 전략
### 1. signup 계정생성 API: 입력으로 ID, PW 받아 내부 DB 에 계정 저장하고 토큰 생성하여 출력  
대표 소스 : [AuthController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/AuthController.java), 
[UserRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/UserRepository.java), 
[CryptoService.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/service/CryptoService.java)  
```
POST /auth/signup  
```
RequestBody로 아이디와 패스워드를 받습니다.  
Validation Pattern을 사용하여 아이디는 5자리 이상 영문자, 숫자 조합, 첫 글자는 영문자.  
패스워드는 영문 2개 이상 숫자, 특문 각 1회 이상, 총 6자리 이상이어야 됩니다.  
```
# RequestBody 예시
{
	"id": "hyoseo",
	"password": "abcd12#"
}
```
패스워드 암호화 알고리즘은 HmacSHA256을 사용하였습니다.  
비밀키는 내부에 저장된 키값과 사용자 아이디를 합쳐서 사용합니다.(같은 비밀번호라도 다른 값이 나오게 하기 위해서 입니다)  
계정 생성이 완료되었으면 JWT를 발급합니다. 해당 JWT의 만료 시간은 생성된 시간으로부터 1시간 뒤입니다.  
claim에는 유저 아이디를 넣어 이후에 각 서비스에서 DB조회 없이 유저 아이디를 알 수 있도록 합니다.  
#### /auth/signup, /auth/signin 등을 제외한 API 호출 시에는 RequestHeader에 유효한 Access-Token이 존재해야 합니다.  
#### 유효한 토큰이 존재하지 않을 때에는 AuthInterceptor에서 막히게 됩니다.  

### 2. signin 로그인 API: 입력으로 생성된 계정 (ID, PW)으로 로그인 요청하면 토큰을 발급한다.
대표 소스 : [AuthController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/AuthController.java), 
[UserRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/UserRepository.java), 
[CryptoService.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/service/CryptoService.java)  
```
POST /auth/signin  
```
RequestBody로 아이디와 패스워드를 받습니다.  
```
# RequestBody 예시
{
	"id": "hyoseo",
	"password": "abcd12#"
}
```
계정이 존재하고 암호화한 패스워드가 유저의 존재하는 계정 패스워드와 일치할 시 JWT를 발급합니다.  
### 3. refresh 토큰 재발급 API: 기존에 발급받은 토큰을 Authorization 헤더에 “Bearer Token”으로 입력 요청을 하면 토큰을 재발급한다.  
대표 소스 : [AuthController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/AuthController.java), 
[UserRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/UserRepository.java), 
[CryptoService.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/service/CryptoService.java), 
[AuthInterceptor.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/interceptor/AuthInterceptor.java)  
```
POST /auth/refresh  
```
```
# RequestHeader 예시
"Access-Token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJoeW9zZW8iLCJ1aWQiOiJoeW9zZW8iLCJpYXQiOjE1ODAwMjExMTksImV4cCI6MTU4MDAyMTQxOX0.Kemmk6aXBvfHRehMTSa5qX0OsBMRkrEhyqqfr4LdUd"
"Authorization":"Bearer Token"
```
JWT를 재발급해 줍니다.  

### 4. 데이터 파일에서 각 레코드를 데이터베이스에 저장하는 API 개발
대표 소스 : [DataFileController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/DataFileController.java)
```
POST /data/csv_files  
```
**멀티파트로 csv_file을 받고 RequestParam으로 bank_name_end_chars를 받습니다.**  
**기본으로 아무 파일도 안주고 bank_name_end_chars도 주지 않아도 됩니다.**  
csv_file이 주어지지 않을 경우 예제로 주어진 사전과제3.csv를 내장하고 있어서 자체적으로 읽어서 처리합니다.  
bank_name_end_chars도 기본 값으로 1,(를 처리합니다.  
</br>
Scanner를 이용하여 한 줄씩 읽으며 ,를 delimiter로 활용하여 한 열씩 자릅니다.  
첫 줄에 은행들 이름이 있는데 bank_name_end_chars는 이 은행 이름만을 추출하는데 사용됩니다.  
새로운 csv 파일에서 은행 이름 추출하는데 다른 문자가 활용될 수 있기에 RequestParam으로 받을 수 있게 처리하였습니다.  
그 다음은 데이터 행들을 추출하여 DB에 넣어주는데 2016년부터의 데이터에는 "와 ,가 들어간 열이 존재합니다.  
이를 처리하기 위해 열이 "로 시작할 경우 다음 열이 "로 끝나기 전까지 열들을 합칩니다.  
한 열은 한 번만 조회되며 행에서 열들을 추출하는데 걸리는 시간복잡도는 O(n)입니다.  
이렇게 추출된 데이터들은 institutes 테이블과 institute_supports 테이블에 각각 들어가게 됩니다.  
  
### 5. 주택금융 공급 금융기관(은행) 목록을 출력하는 API 개발  
대표 소스 : [InstituteController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/InstituteController.java), 
[InstituteRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/InstituteRepository.java)  
```
GET /institutes
```
institutes 테이블에 있는 항목들을 전부 출력합니다.  
  
### 6. 년도별 각 금융기관의 지원금액 합계를 출력하는 API 개발  
대표 소스 : [InstituteController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/InstituteController.java), 
[InstituteSupportRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/InstituteSupportRepository.java)  
```
GET /institutes/total_institutes_support
```
```
@Query("SELECT InsSupport.year, InsSupport.institute, SUM(InsSupport.supportAmount) " +
        "FROM InstituteSupport InsSupport GROUP BY InsSupport.year, InsSupport.institute")
List<Tuple> findYearlySupport();
```
institute_supports 테이블에서 연도와 기관으로 그룹으로 묶은 후 연도, 기관, 해당 연도 그 기관의 지원금액 합계를 추출합니다.  
그리고 핸들러에서 그 결과의 형식을 변경하여 출력합니다.  
  
### 7. 각 년도별 각 기관의 전체 지원금액 중에서 가장 큰 금액의 기관명을 출력하는 API 개발  
대표 소스 : [InstituteController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/InstituteController.java), 
[InstituteSupportRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/InstituteSupportRepository.java)  
```
GET /institutes/top_institute_support
```
```
@Query("SELECT InsSupport.year, InsSupport.institute, SUM(InsSupport.supportAmount) " +
        "FROM InstituteSupport InsSupport GROUP BY InsSupport.year, InsSupport.institute " +
        "ORDER BY SUM(InsSupport.supportAmount) DESC")
List<Tuple> findYearlyTopSupportInstitutes(Pageable pageable);
```
institute_supports 테이블에서 연도와 기관으로 그룹으로 묶은 후 연도, 기관, 해당 연도 그 기관의 지원금액 합계를 추출하며  
지원금액 합계 내림차순으로 정렬합니다.  
이 결과를 PageRequest.of(0, 1)으로 하나만 추출하게 되면 원하는 결과가 나옵니다.  
  

### 8. 전체 년도(2005~2016)에서 외환은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력하는 API 개발
대표 소스 : [InstituteController.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/controller/InstituteController.java), 
[InstituteRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/InstituteRepository.java), 
[InstituteSupportRepository.java](https://github.com/hyoseo/housing-finance/blob/master/src/main/java/me/hyoseo/housingfinance/database/repository/InstituteSupportRepository.java)  
```
GET /institutes/institute_avg_min_max_support
```  
RequestParam으로 bank를 받습니다. 기본 값은 외환은행으로 되어 있으며 국민은행과 같은 다른 값을 줄 수 있습니다.  
다른 은행 명을 주면 그 은행의 전체 년도 지원금액 평균 중에서 가장 작은 금액과 큰 금액을 출력합니다.  
```
@Query("SELECT InsSupport.year, InsSupport.institute, AVG(InsSupport.supportAmount) AS support_avg FROM " +
        "InstituteSupport InsSupport where InsSupport.institute.code = :institute_code " +
        "GROUP BY InsSupport.year, InsSupport.institute ORDER BY support_avg DESC")
List<Tuple> findYearlyAvgSupport(@Param("institute_code") Integer instituteCode);
```
institute_supports 테이블에서 특정 기관 데이터만에서 연도와 기관으로 그룹으로 묶은 후  
연도, 기관, 해당 연도 그 기관의 평균지원금액을 추출하며 평균지원금액 내림차순으로 정렬합니다.  
핸들러에서 결과 리스트의 제일 앞과 제일 뒤의 데이터만을 뽑아 형식을 변경하여 출력합니다.  


