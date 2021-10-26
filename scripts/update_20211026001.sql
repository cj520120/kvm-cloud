ALTER TABLE `roamblue_cloud_management`.`tbl_calculation_scheme`
ADD COLUMN `scheme_cpu_socket` INT NOT NULL DEFAULT 4 AFTER `scheme_cpu_speed`,
ADD COLUMN `scheme_cpu_core` INT NOT NULL DEFAULT 2 AFTER `scheme_cpu_socket`,
ADD COLUMN `scheme_cpu_threads` INT NOT NULL DEFAULT 2 AFTER `scheme_cpu_core`;