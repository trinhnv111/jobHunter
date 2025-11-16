package trinhnv.springRestfull.util.response;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetaResponse {

    private int page;        // trang hiện tại (1-based)
    private int pageSize;    // số phần tử mỗi trang
    private int pages;       // tổng số trang
    private long total;      // tổng số phần tử
}
