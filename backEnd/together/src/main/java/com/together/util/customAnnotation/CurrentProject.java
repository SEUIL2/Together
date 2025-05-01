package com.together.util.customAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentProject {
    boolean required() default true;
}
/*
커스템 어노테이션 사용
    컨트롤러 진입 전에 @CurrentProject 를 통해 projectId를 Resolver 가 주입하며,
    내부적으로는 로그인한 학생의 mainProject 에서 값을 추출해줌.
    교수의 경우엔 입력받은 projectId를 그대로 사용
*/
/*
@CurrentProject 사용시 보통은 RequestParam 으로 사용하는것
 */