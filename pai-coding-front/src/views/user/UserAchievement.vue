<template>
  <!-- 文章勋章板块 -->
  <div class="card">
    <div class="card-body achievement-wrap">
      <div class="achievement-title ">
        <span class="underline font-bold">{{user.userHome.userName}} </span>
          的个人成就
      </div>
      <div class="achievement-item">
        <div class="flex">
          <span>
           <img
             src="data:image/png;base64,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"
             class="iconSize___29n8N"
             _nk="oLTI31"
             style="height: 100%"
           />
          </span>
          <span>已发布文章</span>
        </div>
        <span>{{user.userHome.articleCount + ' 篇'}}</span>
      </div>
      <div class="achievement-item">
        <div class="flex">
          <span>
            <img
              src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAABGZJREFUeF7tmntIU1Ecx7e7O1vrMZeG9IAcFUlh0CrSsrf9kSCrCNIkUtvD+cBFKdX6rxcU0Xpozi2zTKyQ3laQZC+hoCcVQTlHpBhlmj1cbmuLE10Sqd3fubft3rXrX8L9nt/9/T6/3zm/c86dWBThf+IIj18kABAqIMIJCFMgwgtAWASFKSBMgQgnwIspcKjAPG6yu/FxnNehQPloIWc6nFJ1jql8x51g54cXAC7olr5K9DROGBjsXenKm5m2ugXBhMALAM5sqf9vQT6ISn+0svKMOlgQeA8ABR7MSuAcgCXXnKLx7b5Nl+ErZMH6fLvFQqfDfc45gHK9ybTUXbaPznGnVP1xke2ekk6H+5xzALa8wl2p36ybII43DCmNLyzb8RqihWo4B1CvX/FwuvviNIjDl0hTRpF9zymIFqrhHMB13axuledhNMThxsH6nbrDZWaIFqrhFIAl35yi6aVfAKlg/jsAdfrMG0nu+vnQbF2V5RcbK/YfgOohOs4qAGV/ict6S+7vAfvw3yyCBw0lq5K99SdivreRkCwhzQdirHdGlVMK1UN1YPpQg4F06NAT42k9hlP2lL0nZGrLMvuVif/Cj/42QgagRr/2rNrToMEp+f6OBmMBRPaxAKAMkt5Pa4b5u2YT4u+DB2bji0/5tC9qxN6BmxWcVscmw+1EguslmVybW1mpg9oBA0DBK9xt+0iiL+B21OeVuLpl8UYKAu5KD3U8kO6mdPW5bNux5RBbYAAVhqKtCtH7xRCjLv/wFioL99epPDiLHcQ+naZXrPA3yQ0qyLYZDOCoQWeXiT6Pp3s5eo6qIOtIbRr6P9BZH2KLqaYpKscOmQpgAMfzck9I/b1joA5lWk8u5BIAdNEEAziiMx6QE92J4QLgPFE611RFf6cIBmDTF+4aKu5MCgcAqBukVD2VQ3wFAyjXmkxKyVsNxCjScDkFcO4R4QD0G7RKcXtWOADAOTSBARzUlqyKlbzJ4zsA1AKnHO0koH6CAaDLyzipYxvUMFdT4AU5pyPNfmM01E84gOItU+O+te6HGuYKALT/U3GAAfw8yXkd1XwHAG1/2ADQgDpDRhOfAeC0P0YAavVZl/90CvwTFC6mAE77YwSgRrf2DN1pkDLMBQCc9scIAM6BKNQAcNsfIwA42+FQA8Btf4wA4NwJhBoAbvtjBADnPBBqALjtjxEAnO1wKAGwuTIHb4QQsUOGzWkxImcJZC8QSgBsrsyxAOCcB0IJgEn7YzQFcLbDFIDnObE+pt8CIJWGNKpqD1Yi+9vFHgjZDH0VRT/RWitM6EXB/ibApvyRf9gA6KYBuhHukMUbN/76JQfSL/HjfQSFZh5tfq6JDfMgd39/s4kNABlCQSmId9kyye9rchR4n3hIe9egkdup4KmXIv0kSfPpBE/zKGhwgXQo8A5ifM8zf2o6m+AZVcC/CIBPNhhVAJ8CYOuLAIAtwXAfL1RAuGeQrf9CBbAlGO7jhQoI9wyy9T/iK+AHkNrrUMU1Nj0AAAAASUVORK5CYII="
              class="iconSize___29n8N"
              _nk="oLTI31"
              style="height: 100%"
            />
          </span>
          <span>文章被点赞</span>
        </div>
        <span>{{user.userHome.praiseCount + ' 次'}}</span>
      </div>
      <div class="achievement-item flex">
        <div class="flex">
          <span>
            <img
              src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAABEFJREFUeF7tW2tIU2EYfndxc2VuMy3Fwm6gkGAqahGuH1kGRQUZlUJWP1IrozD8FRFRf6JIqKwR0gUyKvsRVJTZD6ddpCITCaKLCqlFmZs53eYu8SajsYtn33fO2Yb7zq/B+b73vO/zPu/le8+OBKL8kkS5/cAAYAyIcgRYCAQigMt1XD6dyCGRHLf7sycgAxgAjAEsBFgOYElwGiHAqgArg6wPYI1QWDvBhurhatWEUTfb2VeQ5OhJinWZZTNcpn+d6JhE7TJJki0jskTjkDStY1SiMey9pD0vZA4OSxK8ctC6Um3rPZFpe1roNjZYo4Zk8+z9soy+QVXOnoq6mBfB7gu0LqQAoLcXj788tsDxTsNXcdzfpSj6wheIkABwvdaeFm/qbsq2PVwmhOHeMvgAIToANyu/NmXbHm0kpTopUBga3Yqi6+X1qZUke0UDAL2ebGxvS594nkKiEN+1yIZhdW5x+Wl5XzCyRAEAk1yuubElwTkQloMTsuFNXOnqYJKk4ABgolthvnmWhvJSFUBMKoA8YdJ39t8A1s/B+NF3DZbQjthtp3fXJx2dSoKgANyo6tfrLNf2kKqMhs9aBRBfCIC/PS/7MMBoB8CfNgDnOKlkgOfKHffLLi8qEb0M3qn8+Czf2qQjVRG9PXc/gFw79U4E4sfFSVaQXi8VJYYd+vTVfjs+IQ5DtMajt1OOcBvvVhxBGDwjLBN4hwAt7dEo7WaAeELOGB8DmJpJOTC5vlW1q8G7TPICQH/YvLXYWNdIpw7A/FO+Mc8lC/NA/0k6FqDsJ5pDpRXnZt51P4caAL6lTrkEIHkfl7n+73+vp68O3iWSGoB7Fe9f59oeULe2M/MBErfTAfCrEcD8hm4v7nqr2NC5RZ+Vh78ZALRVIOpDAOnDJwliCUw9Sp4EsQ/AJEh7CZYE3QrwKYPqdQCatWSmRFQZdKt+p+pjS76laRWZKZPeT6n53/tz7UfvD5wBcFm4Vvref6Uoad2uTy/yvkOdBL0F0YIg0wAkH+AGAY3/fgHAYRTOeF5VwJ8aOPxYab21iVRFZIK6GCAuzzcnYOMz0gowYqDzvL/uz1M/wRjgFnp138+TBZbbtTTHYZSBDZLncdj2jc5wPA63K8tquKbIggOARvAtkaQM8l4f1oGIWxkEYY6lsyFromUxX4NI9kfESMxTYSyTmbbmnWKPx5DynYr190svL9xKApgoIeCtgNhswN5+VL20JNhBqKhJcCr0hQaiJybH+Em2/ARXoptKp5AwIBAj5js+pJGGBlK9K2aNwaRadCyYqS9XOIQFAE+lcIqsnDDqkpx9BXGOIY3WNRjr+XJ0XDrLMSDN6DVJ53SPTZeXo1xeCeX9sDMglMb6exYDgHYgEm7PCfV8xgDGAPYvMfYvMb/VIVCSYd8LsO8Fovx7AaHqb6TLYV+NRbqHxNaPMUBshCNdftQz4C8AmnxferWMjwAAAABJRU5ErkJggg=="
              class="iconSize___29n8N"
              _nk="oLTI31"
              style="height: 100%"
            />
          </span>
          <span>文章被阅读</span>
        </div>
        <div>
          <span>{{user.userHome.readCount + ' 次'}}</span>
        </div>
      </div>
      <div class="achievement-item">
        <div class="flex">
          <span>
            <img
              src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAABHxJREFUeF7tmmtIU1EAx3W7m84QswJLjbRVGDGqFTVolcW+WGRP8AGRD66zzLIPRbEPfYge1IesfG9qEWiBZU8kGD7KwKDMEjFS0UqTpJUSOfVuu3GIWyK2e+69Z+fe3N3Xe17/3/mfe//nnAUG+Pkv0M/1B8gAZAf4OQF5Cfi5AeSXoGhL4Jr5eNIKz9MS4MAOxabs3NJLt8Vwo2gAXmbGUnPd/QQQ7VBGu1qCU5ccLjz7ATcEUQBUZ6U0GiZqNk8W2xK0tzGl9NaWGQ8g/5DFaByvamBmnxE8GhhGN4SYY3G7ALsDHpAJXTrKvmS6mRbDBVgBgNnfOXrxmTeb3w85sTGv6GwzrqWAFYC32WcE96r0w1utL8JnHACY2WdE1xE5xw7Z8vNxQMDmgHpy/fdYqnU2jCicLsACoNJM2uLHr2fCiMftAiwAJoceWAggHK0t71XBludbzucA+Mw+I6ZJlXovzXpjN19xMPV8CqAoKy9vnfv+pamhB2ZgoAwIR/Xq9BRf7hOQAcjPsBjDVENpobRDP4ceiI1w9YSF0CNI2gcgvhDakW+BUb3fFZFPxmhNGyoonAdYkGNZRLsmDLM9Q0mh9JBunqc/KpweDEYlFtYdoBz4WgAoTjq07wcd/tQTNKuWa5SGAgBEz6c6a2Pcr3V87cxFmJCyA4o4p0MZPfBJrTfBwIACAJPghAzaF3VfqXe83ld2V8/WNhSAOjL+cxz1fAFbY1J63qPUD5vK2SM1FABr9uHzprHSk1ISyDYWe7D5AllScIqtHBQA0Ei1ObnBMH4nnq1BKTznsq2GBvC/QOAiHmjiBEDqELiK5wVAqhD4iOcNAFS8RyZ0r6TsWims+XaVqTvRWreUz1g4L4HJnUgBghDxghzAgGjO0I1Ged5p+NAXWgekPmNFe4iQdgQ5AHQMYvIq54NO3BCA+DZN4nKYuOsNkGAADASDs6p7ruf3TY+vfw5FtKtFg+YmCQkAIBhnWrRrss6RxYUWFKBlACgogjZukgfuGqkqnx5f/XnxqlJr91tv7EExdmQOqMna07pm4uFqFINiawN2q8vWDpLPINMJzi1zJ7FhcJutMRJGIFsZZA7AmQdQfP8ZMMgAdKTP8+A6FwSHpCsqvyrYZhfmORIAIAxt/3mxD6ZDVGUezzoRIzQEIXsHgPP/hInCy6jEwbTziMhLzrUJ/18REgfgDEEMnAZ1ui2jrIyEgeXzKMw1A4A1/FZtahpSai30WIBisbK1MtL9LoZLlG5GlAWQOAA2AzDCHYQ2ber6BTdLEaqeM0upF0YYEKiyABIAbHf/3oRPZ09woaqj7Ae8gUCVBZAA+FcGAGfzH5Ura/iuVW8gwI5wbYXw63MkAKamQCC8S2U4fbDkylWhLylQH4BY6Hq7V+v++w+TN4Spe5eN3zHY5DEhAcDcHRIeZ+ggsewaKuFT4RXnHD2yYPx9rlMZ1j/de4QPbCQA+HQslToyAKnMhFjjkB0gFnmp9Cs7QCozIdY4ZAeIRV4q/coOkMpMiDUOv3fALy+Y7VBeOpb5AAAAAElFTkSuQmCC"
              class="iconSize___29n8N"
              _nk="oLTI31"
              style="height: 100%"
            />
          </span>
          <span>文章被收藏</span>
        </div>
        <span>{{user.userHome.collectionCount + ' 篇'}}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import type { UserHomeInfoResponseType } from '@/http/ResponseTypes/UserHomeInfoResponseType'

const props = defineProps<{
  user: UserHomeInfoResponseType
}>()

</script>

<style scoped>

</style>
