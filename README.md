# Gamsi Bot

YouTube 채널의 구독자 수가 유저가 설정한 값에 도달한 순간 그 채널의 스크린샷을 찍어서 유저가 지정한 이메일 주소로 전송해주는 RESTful API 서버입니다.

## 이용 방법

[핸들](https://www.youtube.com/watch?v=m0kWGFNlPKA)이 `@user`인 YouTube 채널의 구독자 수가 1000에 도달할 때 스크린샷을 `example@gmail.com`으로 전송하는 예시:

```sh
$ curl -d '{"handle":"user","targetSubscriberCount":1000,"emailAddress":"example@gmail.com"}' \
-H 'Content-Type: application/json' \
-D - \
https://app.gamsi-bot.com/requests
```

```
HTTP/2 200 
date: Mon, 26 Dec 2022 14:09:26 GMT
content-type: application/json
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY

{"id":11,"handle":"user","targetSubscriberCount":1000,"emailAddress":"example@gmail.com","createdAt":"2022-12-26T14:09:22.002529441Z"}
```

- id: 유저가 서버에 전송한 요청에 부여된 고유 번호.
- 해당 채널의 구독자 수가 이미 1000명 이상일 경우에는 아래와 같은 응답과 함께 요청이 거절됩니다.

```
HTTP/2 400 
date: Mon, 26 Dec 2022 09:35:11 GMT
content-type: application/json
content-length: 62
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY

{"message":"The target subscriber count is already achieved."}
```

---

아이디 `user`, 비밀번호 `Abcd1234`로 회원가입하는 예시:

```sh
$ curl -d '{"username":"user","password":"Abcd1234"}' \
-H 'Content-Type: application/json' \
-D - \
https://app.gamsi-bot.com/signup
```

```
HTTP/2 200 
date: Mon, 26 Dec 2022 14:52:40 GMT
content-type: text/plain;charset=UTF-8
content-length: 5
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY

user
```

---

아이디 `user`, 비밀번호 `Abcd1234`로 로그인하는 예시:

```sh
$ curl -d '{"username":"user","password":"Abcd1234"}' \
-H 'Content-Type: application/json' \
-D - \
https://app.gamsi-bot.com/login
```

```
HTTP/2 200 
date: Mon, 26 Dec 2022 09:01:57 GMT
content-length: 0
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNjcyMTMxNzE3fQ.rGfOwWfKI1L2Vrk310dFQBVSXL4NqD5UxgWiG3mr_uc
access-control-expose-headers: Authorization
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY
```

- authorization 헤더: 로그인한 회원으로서 요청을 보낼 때 사용하는 JWT.

---

**로그인한 상태에서** 핸들이 `@user`인 YouTube 채널의 구독자 수가 1000에 도달할 때 스크린샷을 `example@gmail.com`으로 전송하는 예시:

```sh
$ curl -d '{"handle":"user","targetSubscriberCount":1000,"emailAddress":"example@gmail.com"}' \
-H 'Content-Type: application/json' \
-H 'JWT: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNjcyMTMxNzE3fQ.rGfOwWfKI1L2Vrk310dFQBVSXL4NqD5UxgWiG3mr_uc' \ # 추가된 부분
-D - \
https://app.gamsi-bot.com/requests
```

```
HTTP/2 200 
date: Mon, 26 Dec 2022 14:09:26 GMT
content-type: application/json
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY

{"id":11,"handle":"user","targetSubscriberCount":1000,"emailAddress":"example@gmail.com","createdAt":"2022-12-26T14:09:22.002529441Z"}
```

- 로그인한 상태에서 서버에 요청할 경우 나중에 자신이 서버에 전송한 요청을 열람할 수 있습니다.

---

자신이 서버에 전송한 요청을 열람하는 예시:

```sh
$ curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNjcyMTMxNzE3fQ.rGfOwWfKI1L2Vrk310dFQBVSXL4NqD5UxgWiG3mr_uc' \
-D - \
https://app.gamsi-bot.com/requests
```

```
HTTP/2 200 
date: Mon, 26 Dec 2022 16:41:30 GMT
content-type: application/json
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY

[{"id":11,"handle":"user","targetSubscriberCount":1000,"emailAddress":"example@gmail.com","createdAt":"2022-12-26T14:09:22.002529441Z"}]
```

---

`id`가 11인 요청을 삭제하는 예시:

```sh
$ curl -X DELETE \
-H 'JWT: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNjcyMTMxNzE3fQ.rGfOwWfKI1L2Vrk310dFQBVSXL4NqD5UxgWiG3mr_uc' \
-D - \
https://app.gamsi-bot.com/requests/11
```

```
HTTP/2 200 
date: Mon, 26 Dec 2022 14:35:04 GMT
content-type: application/json
vary: Origin
vary: Access-Control-Request-Method
vary: Access-Control-Request-Headers
x-content-type-options: nosniff
x-xss-protection: 1; mode=block
cache-control: no-cache, no-store, max-age=0, must-revalidate
pragma: no-cache
expires: 0
x-frame-options: DENY

{"success":true}
```

- 로그인하지 않은 유저가 서버에 전송한 요청은 `JWT` 헤더 없이도 삭제할 수 있습니다.
- `success`가 `false`인 경우 삭제하려는 요청이 이미 처리됐을 가능성이 있습니다.