package Project.ProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "item_category")
public class ItemCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @NotEmpty
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private ItemCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<ItemCategory> children = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Item> items = new ArrayList<>();

    @Column(name = "sort_order")
    private Integer sortOrder;

    public void addChildCategory(ItemCategory child) {
        children.add(child);
        child.setParent(this);
    }

}

