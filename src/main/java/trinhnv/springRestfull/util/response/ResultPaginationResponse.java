package trinhnv.springRestfull.util.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultPaginationResponse<T> {

    private MetaResponse meta;
    private List<T> result;

    public static <E, D> ResultPaginationResponse<D> ok(
            Page<E> page,
            Function<E, D> mapper
    ) {
        ResultPaginationResponse<D> rs = new ResultPaginationResponse<>();

        MetaResponse meta = new MetaResponse();
        meta.setPage(page.getNumber() + 1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);

        rs.setResult(page.getContent().stream().map(mapper).toList());
        return rs;
    }
}
