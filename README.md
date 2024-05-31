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