package org.example.container;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

import java.util.Set;

/**
 * 서블릿 컨테이너 초기화 코드
 * 서블릿 컨테이너 초기화 코드 안에 애플리케이션 초기화 코드를 실행하는 for문이 있다.
 * appInit.onStartup(ctx) 이 부분이 애플리케이션 초기화를 하는 코드 호출하는 부분
 * 굳이 애플리케이션 초기화 코드와 서블릿 컨테이너 초기화 코드를 나누는 이유는 편의성은 살리고 의존성을 최대한 줄이기 위함
 * 의존성을 줄인다는 의미는 서블릿 컨테이너 초기화는 ServletContainerInitializer를 구현해야 함.
 * 애플리케이션 초기화는 굳이 그럴 필요가 없음. 그리고 AppInit과 같은 인터페이스를 자유자재로 만들 수 있음
 * {@code @HandlesTypes}는 ()안에 해당 인터페이스를 구현한 구현체는 모두 애플리케이션 초기화 대상으로 생각하고 초기화해준다는 애노테이션이다.
 * */
@HandlesTypes(AppInit.class)
public class MyContainerInitV2 implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        System.out.println("MyContainerInitV2.onStartup");
        System.out.println("c = " + c);
        System.out.println("ctx = " + ctx);

        for (Class<?> appInitClass : c) {
            try {
                // new AppInitV1Servlet()과 똑같은 코드라고 보면 된다.
                AppInit appInit = (AppInit) appInitClass.getDeclaredConstructor().newInstance();
                appInit.onStartup(ctx);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
