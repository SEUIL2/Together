// backEnd/together/src/main/java/com/together/ProjectDetail/develop/DevOrderItemRepository.java

package com.together.ProjectDetail.develop;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface DevOrderItemRepository extends JpaRepository<DevOrderItemEntity, Long> {


    List<DevOrderItemEntity> findByProject_ProjectId(Long projectId);
}
