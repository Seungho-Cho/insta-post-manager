package com.plamason.postmanager.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GroqTagSuggestServiceImplTest {

    @Autowired
    private GroqTagSuggestServiceImpl groqTagSuggestService;

    String apiKey = "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";
    String llmModel = "llama-3.3-70b-versatile";

    String content1 = "KF-16DU Republic Of Korea Air Force, 120th Fighter Squadron\n" +
            "\n" +
            "1/32 Academy\n" +
            "\n" +
            "It's been a while since I post the last build. Academy 1/32 KF-16DU CMPL.\n" +
            "\n" +
            "KF-16DU is a upgraded ver. of the KF-16D. Improved fire control radar and avionics made them much capable for protecting our country.\n" +
            "\n" +
            "Detail ups\n" +
            "\n" +
            "- Aires Wheelbay Set\n" +
            "- Aires AOA Cover set\n" +
            "- Reskit Wheel set\n" +
            "- Reskit Aim-120C-7\n" +
            "- Reskit Aim-9X\n" +
            "- Reskit PW-229 Exhaust set\n" +
            "- Wolfpack Design Cockpit set";

    String content2 = "1/350 IJM Myoko 1942 \n이번 2025 IPMS에 출품했던 일본 중순양함 묘코입니다.\n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "아오시마 제품과 플라이호크 디테일업 세트 + 범용 에칭을 가지고 완성하였습니다.\n" +
            "\n" +
            "1/350 IJN myoko\n" +
            "아오시마의 350 중순 시리즈 타카오급, 묘코급의 묘코이다. 전작 타카오에서 욕을 바가지로 먹고 항문에 힘주고 나온 제품이라 그냥 만들어도 그럴듯한데 마침 홍콩럭키에 플라이호크 묘코가 세일하길래 그걸 집어서 만들었다. (사실 묘코도 세일때 산거라 ㅋ) 이번에 아오시마에서 타카오급 리뉴얼로 전면개수해서 나왔으니 그것도 한번 만들어보고싶군. 스탠드도 들어있는거라 별매는 플라이호크 묘코셋과 라이언로어 .3mm 황동봉 정도이다. 색칠은 저번 야마토와 같이 삼화페인트 가정용락카 백/흑의 조합과 방청도료적색, 리놀륨 갑판은 바예호 아크릴 레자...\n" +
            "\n" +
            "그리고 과거에 묘코를 완성하셨던 gmmk11님의 작례도 많이 참고하였습니다.\n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "똥킷 집합소라 악명높은 브랜드 아오시마 치고는 \n" +
            "\n" +
            "지금 만들어도 나름 괜찮은 물건이었습니다.\n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "09년도에 나온 금형이며, 인지도 높은 함급이라 그런지 \n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "묘코급 4자매 (묘코, 나치, 아시가라, 하구로)가 모두 발매되어 있고,\n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "플라이호크 에칭 또한 4자매를 만들 수 있는 범용성있는 에칭을 제공하므로\n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "입맛대로 작업하시면 될 거 같네요.\n" +
            "\n" +
            "\u200B\n" +
            "\n" +
            "사양은 아오시마에서 제시한 42년 초기 사양으로 작업했습니다.";

    String prompt = "난 스케일모델(프라모델) 인스타그램을 운영하고 있는데, " +
            "다음 제목과 내용을 통해서 인스타그램 포스팅용 해시태그를 여러개 추천해줘. " +
            "영문 위주로, 중요도가 낮은건 제외하고 포스팅의 노출도를 높일수있는 핵심 태그로 20개 까지:\n\n";


    @Test
    void apiTest() {
        System.out.println(groqTagSuggestService.suggestTag(content2));
    }
}
