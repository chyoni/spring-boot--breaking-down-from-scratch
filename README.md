# 스프링 부트 깨부시기

## 스프링 부트가 해주는 것의 대표적인 것들

- 내장 톰캣 서버
- 스프링 부트 스타터와 라이브러리 관리
- 자동 구성 (Auto Configuration)
- 프로덕션 준비 기능 (액츄에이터, 모니터링 기능)

## 스프링 부트가 대신 해주는 것들에 대한 직접 구성 및 구현으로부터 본질 이해하기
- [x] 직접 톰캣 서버 설치하고 작업해보기
  - 톰캣 로컬에 설치하기
  - 서블릿 컨테이너 초기화 직접 해보기
  - 서블릿 컨테이너 초기화 직접 한 후 애플리케이션 초기화도 직접해보기
  - 스프링 컨테이너 등록 직접해보기
  - 스프링 MVC가 지원해주는 서블릿 컨테이너 초기화에 애플리케이션 초기화만 얹기
- [x] 내장 톰캣을 사용한 편리함 느껴보기
```groovy

//일반 Jar 생성
tasks.register('buildJar', Jar) {
  manifest {
    attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
  }
  with jar
}

// FatJar
/**
 * Fat Jar는 뭐냐?
 * 스프링 부트는 내부에 톰캣까지 실행해주는 아주 큰 편리함을 제공한다.
 * 스프링 부트가 없던 시절엔 직접 톰캣을 설치하고 WAR 파일로 그 톰캣 서버위에 배포해서 실행했다.
 *
 * 그럼 스프링부트는 어떻게 내장 톰캣을 사용하는거지?로 시작하는거다.
 * 그래서 직접 내장 톰캣을 사용해보기 위해 위 의존성 'org.apache.tomcat.embed:tomcat-embed-core:10.1.5'를 다운받고
 * 직접 코드레벨로 내장 톰캣을 실행했다. 그럼 이제 내장 톰캣을 포함하는 Jar 파일을 만들면된다. 그리고 시작지점인 메인 클래스를 지정해주면 된다.
 * 그게 바로 위에 buildJar.
 * 근데!!,
 *
 * 위에 buildJar를 실행하면 사용하는 라이브러리를 포함시킬 수가 없다.
 * 왜냐하면 Jar안에 Jar는 포함시킬수가 없기 때문에. 그럼 사용하는 라이브러리들을 포함 못시키니까 애플리케이션이 정상적으로 실행될리 없다.
 * 어떤 방법이 있을까?해서 나온 불완전한 방법 중 하나가 이 FatJar. Jar안에 Jar를 포함시킬 순 없지만, Jar안에 .class 파일은 얼마든지 포함시킬 수 있다.
 * 그래서 외부 라이브러리의 모든 클래스 파일을 다 꺼내서 하나의 Jar에 포함시키는 방법이 바로 이 Fat Jar. 말 그대로 뚱뚱한 Jar.
 *
 * 그럼 외부 라이브러리도 다 클래스 파일로 포함시키면 애플리케이션에서 필요한 클래스를 찾아올 수 있으니까 애플리케이션은 잘 동작한..다?
 * 이 방법은 크나큰 문제가 있다. 외부 라이브러리 중 이름이 같은 클래스파일이나 기타 설정 파일이 있다면? 둘 중 하나는 버려진다(덮어쓰기).
 *
 * 이게 가장 큰 문제다. 결국 반쪽짜리 해결책인 것이고 이것을 스프링 부트는 어떻게 해결할까?
 * */
tasks.register('buildFatJar', Jar) {
    manifest {
        attributes 'Main-Class': 'hello.embed.EmbedTomcatSpringMain'
    }
    duplicatesStrategy = DuplicatesStrategy.WARN
    from { configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```

## 스프링 부트는 어떻게 Jar 파일안에 Jar 파일을 추가할까?
```groovy
/*
일반적으로 Jar 파일 안에는 Jar 파일을 넣을 수 없다.
근데 스프링 부트로 만든 프로젝트를 build 해보면 Jar 파일안에 무수히 많은 내가 추가한 라이브러리들을 Jar 파일로 제공한다.
어떻게 이게 가능할까?

스프링 부트에서 직접 제공하는 실행 가능한 Jar를 스프링 부트가 제공하기 때문이다.

스프링 부트로 빌드를 해보자. 빌드하면 build/libs에 .jar 파일이 만들어진다.
이 파일을 다음 명령어로 풀어보자.

jar -xvf xxx.jar

풀어보면 META-INF라는 폴더에 MANIFEST.MF 파일이 있다. 이건 자바에서 규칙으로 만들어놓은 파일이고
실행하는 메인 클래스를 지정한 파일과 여러 메타데이터가 존재하는 파일이다.

이 파일을 열어보면 

Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: hello.boot.BootApplication

이런 내용이 있다. 메인 클래스에 내가 만들지 않은 JarLauncher라는 클래스가 있다. 이게 바로 실행 가능한 Jar 이다.

이걸 실제로 실행하면 라이브러리로 필요한 모든 jar를 실행가능한 상태로 만들어준 다음 Start-Class에 설정된
실제 스프링 부트 실행 클래스를 호출해서 스프링 부트가 Jar파일안에 Jar를 넣을 수 있게 해주는 것이다.
*/
```

## 스프링 부트가 관리해주는 외부 라이브러리 버전
- 스프링은 어지간하게 유명하거나 자주 사용되는 라이브러리의 버전을 현재 스프링 부트의 버전과 맞춰 관리를 알아서 해준다.
- 그래서 별도로 라이브러리를 추가할 때 버전을 명시하지 않아도 알아서 최적의 버전으로 다운받아준다.
- 이게 개발자의 개발 속도와 개발자 경험을 높여주는 스프링 부트가 제공하는 큰 장점이다.
- 물론 스프링 부트가 관리하지 않는 라이브러리는 버전을 직접 명시해줘야 한다.
- 근데 여기서 한 발 더 나아가서 스프링 부트 스타터라는 스프링 부트가 만들어준 어떤 용도의 개발을 하기 위해 필요한 기본적인 라이브러리들을 다 포함해
놓은 세트를 제공하는데 예를 들어 spring-boot-starter-web 이라는 라이브러리를 추가하면 웹 애플리케이션을 만들기 위해
필요한 어지간한 모든 라이브러리가 다 들어오게 된다. 그래서 더더 효율적이고 빠르게 개발을 할 수 있다.
- 근데 스프링 부트 스타터를 사용할 때 딸려오는 여러 외부 라이브러리 중 특정 라이브러리의 버전을 바꾸고 싶을때, 그러니까 스프링 부트 스타터가 설정한
버전 말고 다른 버전으로 바꾸고 싶을때가 아주 가끔 존재한다. 그럴땐 다음과 같이 코드를 build.gradle에 추가하면 된다.
```groovy
ext['tomcat.version']='10.1.4'
```
- 이건 톰캣의 버전을 스프링 부트 스타터에서 설정한 버전 말고 직접 어떤 특정 버전으로 선정하는 것이다. 저 
ext['tomcat.version']은 어떻게 알 수 있냐? 다음 링크에서 확인하면 된다.
- https://docs.spring.io/spring-boot/appendix/dependency-versions/properties.html