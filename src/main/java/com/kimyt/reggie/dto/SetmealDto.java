package com.kimyt.reggie.dto;

import com.kimyt.reggie.entity.Setmeal;
import com.kimyt.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
