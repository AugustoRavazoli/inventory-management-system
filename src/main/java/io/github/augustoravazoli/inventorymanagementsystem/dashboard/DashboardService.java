package io.github.augustoravazoli.inventorymanagementsystem.dashboard;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public Dashboard retrieveDashboard(User user) {
        return dashboardRepository.findById(user.getId()).orElseThrow();
    }

}
