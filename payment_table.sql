-- ===================================================================
-- T-MART E-COMMERCE - PAYMENT TABLE
-- Script tạo bảng Payments để lưu thông tin thanh toán
-- Tích hợp VNPay Payment Gateway
-- ===================================================================

USE asmJava6;
GO

-- Drop table nếu đã tồn tại
IF OBJECT_ID('dbo.Payments', 'U') IS NOT NULL
    DROP TABLE dbo.Payments;
GO

-- Tạo bảng Payments
CREATE TABLE Payments (
    paymentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    orderID INT NOT NULL,
    paymentMethod NVARCHAR(50) NOT NULL,           -- COD, VNPAY, MOMO
    transactionID NVARCHAR(100),                   -- Mã giao dịch từ VNPay
    amount BIGINT NOT NULL,                        -- Số tiền (VNĐ)
    paymentStatus NVARCHAR(50) NOT NULL,           -- PENDING, SUCCESS, FAILED, CANCELLED
    paymentDate DATETIME NOT NULL DEFAULT GETDATE(),
    bankCode NVARCHAR(50),                         -- Mã ngân hàng (NCB, VCB, TCB...)
    responseCode NVARCHAR(10),                     -- Response code từ VNPay
    description NVARCHAR(500),                     -- Mô tả giao dịch
    
    CONSTRAINT FK_Payments_Orders FOREIGN KEY (orderID) 
        REFERENCES Orders(orderID) ON DELETE CASCADE
);
GO

-- Tạo indexes để tăng performance
CREATE INDEX IX_Payments_OrderID ON Payments(orderID);
CREATE INDEX IX_Payments_TransactionID ON Payments(transactionID);
CREATE INDEX IX_Payments_PaymentStatus ON Payments(paymentStatus);
CREATE INDEX IX_Payments_PaymentDate ON Payments(paymentDate DESC);
GO

-- Insert sample data (COD payments cho existing orders)
INSERT INTO Payments (orderID, paymentMethod, amount, paymentStatus, description)
SELECT 
    orderID,
    'COD' as paymentMethod,
    amount,
    CASE 
        WHEN status = 4 THEN 'SUCCESS'
        WHEN status = 3 THEN 'CANCELLED'
        ELSE 'PENDING'
    END as paymentStatus,
    N'Thanh toán khi nhận hàng (COD)' as description
FROM Orders
WHERE orderID NOT IN (SELECT orderID FROM Payments);
GO

-- View để xem thống kê thanh toán
CREATE VIEW vw_PaymentStatistics AS
SELECT 
    p.paymentMethod,
    p.paymentStatus,
    COUNT(*) as TotalTransactions,
    SUM(p.amount) as TotalAmount,
    MIN(p.amount) as MinAmount,
    MAX(p.amount) as MaxAmount,
    AVG(p.amount) as AvgAmount
FROM Payments p
GROUP BY p.paymentMethod, p.paymentStatus;
GO

-- View chi tiết payment với order info
CREATE VIEW vw_PaymentDetails AS
SELECT 
    p.paymentID,
    p.transactionID,
    p.paymentMethod,
    p.paymentStatus,
    p.amount,
    p.paymentDate,
    p.bankCode,
    o.orderID,
    o.username,
    o.orderDate,
    o.status as orderStatus,
    o.adress,
    o.phone,
    a.fullname as customerName,
    a.email as customerEmail
FROM Payments p
INNER JOIN Orders o ON p.orderID = o.orderID
INNER JOIN Accounts a ON o.username = a.username;
GO

-- Stored Procedure: Lấy payment history của user
CREATE PROCEDURE sp_GetUserPaymentHistory
    @username NVARCHAR(50)
AS
BEGIN
    SELECT 
        p.*,
        o.orderDate,
        o.adress,
        o.phone,
        o.status as orderStatus
    FROM Payments p
    INNER JOIN Orders o ON p.orderID = o.orderID
    WHERE o.username = @username
    ORDER BY p.paymentDate DESC;
END;
GO

-- Stored Procedure: Thống kê doanh thu theo phương thức thanh toán
CREATE PROCEDURE sp_RevenueByPaymentMethod
    @fromDate DATE,
    @toDate DATE
AS
BEGIN
    SELECT 
        p.paymentMethod,
        COUNT(*) as TotalOrders,
        SUM(p.amount) as TotalRevenue,
        SUM(CASE WHEN p.paymentStatus = 'SUCCESS' THEN p.amount ELSE 0 END) as SuccessRevenue,
        COUNT(CASE WHEN p.paymentStatus = 'SUCCESS' THEN 1 END) as SuccessCount,
        COUNT(CASE WHEN p.paymentStatus = 'FAILED' THEN 1 END) as FailedCount
    FROM Payments p
    WHERE CAST(p.paymentDate AS DATE) BETWEEN @fromDate AND @toDate
    GROUP BY p.paymentMethod;
END;
GO

-- Test query
SELECT * FROM Payments;
SELECT * FROM vw_PaymentStatistics;
SELECT * FROM vw_PaymentDetails;

PRINT 'Payment table và related objects đã được tạo thành công!';
GO
