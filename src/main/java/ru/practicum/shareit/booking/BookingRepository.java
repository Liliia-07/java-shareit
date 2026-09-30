package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime startBefore, LocalDateTime endAfter, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime startBefore, LocalDateTime endAfter, Sort sort);

    @Query("select b from Booking b " + "where b.item.id = ?1 and b.status = 'APPROVED' " + "and b.start < ?2 " + "order by b.end desc")
    List<Booking> findLastBookingForItem(Long itemId, LocalDateTime now);

    @Query("select b from Booking b " + "where b.item.id = ?1 and b.status = 'APPROVED' " + "and b.start > ?2 " + "order by b.start asc")
    List<Booking> findNextBookingForItem(Long itemId, LocalDateTime now);

    List<Booking> findByItemIdAndBookerIdAndEndIsBefore(Long itemId, Long bookerId, LocalDateTime now);
}