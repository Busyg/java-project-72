@import hexlet.code.dto.urls.UrlsPage
@import hexlet.code.util.NamedRoutes
@import hexlet.code.model.Url
@import hexlet.code.model.UrlCheck
@import java.util.Map
@param  UrlsPage page

@template.layout.page(
page = page,
content = @`
    <section>
        @if(page != null && page.getFlash() != null)
            <div class="rounded-0 m-0 alert alert-dismissible fade show alert-${page.getFlashType()}" role="alert">
                <p class="m-0">${page.getFlash()}</p>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="close"></button>
            </div>
        @endif
        <div class="container-lg mt-5">
            <h1>Сайты</h1>
            <table class="table table-bordered table-hover mt-3">
                <thread>
                    <tr>
                        <th class="col-1">ID</th>
                        <th>Имя</th>
                        <th class="col-2">Последняя проверка</th>
                        <th class="col-1">Код ответа</th>
                    </tr>
                </thread>
                <tbody>
                @for(Map.Entry<Url, UrlCheck> urlMap : page.getUrls().entrySet())
                    !{
                        Url url = urlMap.getKey();
                        UrlCheck lastCheck = urlMap.getValue();
                        }
                    <tr>
                        <td>
                            ${url.getId()}
                        </td>
                        <td>
                            <a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()}</a>
                        </td>
                        <td>
                            @if(lastCheck != null)
                                ${lastCheck.getCreatedAt().toString()}
                            @endif
                        </td>
                        <td>
                            @if(lastCheck != null)
                                ${lastCheck.getStatusCode()}
                            @endif
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </section>
`)