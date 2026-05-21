package com.example.iotalarmcopilot.device.infrastructure.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 设备mapper
 */
@Mapper
public interface DeviceMapper {

    // ON CONFLICT (device_code) DO NOTHING 保证幂等
    @Insert("""
            INSERT INTO device (
                device_code,
                product_code,
                device_name,
                group_code,
                status,
                shadow_version,
                shadow_document,
                shadow_updated_at,
                registered_at,
                status_changed_at,
                created_at,
                updated_at
            ) VALUES (
                #{deviceCode},
                #{productCode},
                #{deviceName},
                #{groupCode},
                #{status},
                #{shadowVersion},
                #{shadowDocument},
                #{shadowUpdatedAt},
                #{registeredAt},
                #{statusChangedAt},
                #{createdAt},
                #{updatedAt}
            )
            ON CONFLICT (device_code) DO NOTHING
            """)
    int insertIgnore(DeviceRecord record);

    @Update("""
            UPDATE device
            SET product_code = #{target.productCode},
                device_name = #{target.deviceName},
                group_code = #{target.groupCode},
                status = #{target.status},
                shadow_version = #{target.shadowVersion},
                shadow_document = #{target.shadowDocument},
                shadow_updated_at = #{target.shadowUpdatedAt},
                registered_at = #{target.registeredAt},
                status_changed_at = #{target.statusChangedAt},
                updated_at = #{target.updatedAt}
            WHERE id = #{current.id}
              AND updated_at = #{current.updatedAt}
            """)
    int updateIfUnchanged(
            @Param("current") DeviceRecord current,
            @Param("target") DeviceRecord target);

    @Select("""
            SELECT
                id,
                device_code,
                product_code,
                device_name,
                group_code,
                status,
                shadow_version,
                shadow_document,
                shadow_updated_at,
                registered_at,
                status_changed_at,
                created_at,
                updated_at
            FROM device
            WHERE device_code = #{deviceCode}
            LIMIT 1
            """)
    DeviceRecord selectByDeviceCode(String deviceCode);

    @Select("""
            SELECT
                id,
                device_code,
                product_code,
                device_name,
                group_code,
                status,
                shadow_version,
                shadow_document,
                shadow_updated_at,
                registered_at,
                status_changed_at,
                created_at,
                updated_at
            FROM device
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<DeviceRecord> selectRecent(int limit);
}
