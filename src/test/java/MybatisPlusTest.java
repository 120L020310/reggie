import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class MybatisPlusTest {

//    flavors = flavors.stream()
//            .map((item) -> {
//        item.setDishId(dishId);
//        return item; })
//            .collect(Collectors.toList());
}
