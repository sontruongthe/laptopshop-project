app.controller("ctrlstatistical", function ($scope, $http) {
  $scope.confirm = [];

  $scope.load_all = function () {
    $http
      .get(`http://localhost:8080/statistical/confirm`)
      .then((res) => {
        $scope.confirm = res.data;
      })
      .catch((error) => {
        console.log("Error", error);
      });
  };
  $scope.load = function () {
    $http
      .get(`http://localhost:8080/statistical/confirms`)
      .then((res) => {
        $scope.confirms = res.data;
      })
      .catch((error) => {
        console.log("Error", error);
      });
  };
  $scope.xacnhan = function (orderid) {
    $http
      .get(`http://localhost:8080/statistical/confirm/${orderid}`)
      .then((resp) => {
        $scope.form = resp.data;
        var item = $scope.form;
        item.status = 2;
        console.log(item);

        $http
          .put(`http://localhost:8080/statistical/confirm/${orderid}`, item)
          .then((resp) => {
            Swal.fire("Hệ Thống", "Xác nhận đơn hàng thành công!!", "success");
            $http
              .post(`http://localhost:8080/send/orders`, item)
              .then((ressendOder) => {
                console.log(ressendOder);
              })
              .catch((error) => {
                console.log(error);
                alert("that bai");
              });
            $scope.load();
            $scope.load_all();
          })
          .catch((error) =>
            Swal.fire("Error", "Hình như là mình hết hàng rùi :(", "error")
          );
      })
      .catch((error) => console.log("Error", error));
  };
  $scope.huydon = function (orderid) {
    $http
      .get(`http://localhost:8080/statistical/confirm/${orderid}`)
      .then((resp) => {
        $scope.form = resp.data;
        var item = $scope.form;
        item.status = 3;
        console.log(item);

        $http
          .put(`http://localhost:8080/statistical/confirm/${orderid}`, item)
          .then((resp) => {
            Swal.fire("Hệ Thống", "Đơn Hàng đã được hủy!", "success");
            $http
              .post(`http://localhost:8080/send/orders`, item)
              .then((ressendOder) => {
                console.log(ressendOder);
              })
              .catch((error) => {
                console.log(error);
                alert("that bai");
              });
            $scope.load();
            $scope.load_all();
          })
          .catch((error) => Swal.fire("Error", "Xác nhận rùi mà :(", "error"));
      })
      .catch((error) => console.log("Error", error));
  };

  $scope.info = function (orderid) {
    $http
      .get(`http://localhost:8080/statistical/infoDetail/${orderid}`)
      .then((resp) => {
        $scope.items = resp.data;
      })
      .catch((error) => console.log("Error", error));
  };
  $scope.loadData = async function () {
    try {
      await $scope.fetchAllSanPham(); // Đợi dữ liệu sản phẩm tải xong
      await $scope.fetchTop5Items(); // Đợi dữ liệu top 5 tải xong
    } catch (error) {
      console.error("Lỗi khi tải dữ liệu:", error);
    }
  };

  $scope.fetchAllSanPham = async function () {
    try {
      const response = await $http.get(`http://localhost:8080/statistical/allSanPham`);
      $scope.allSanPham = response.data;
      console.log("Dữ liệu sản phẩm đã tải:", $scope.allSanPham);
    } catch (error) {
      console.error("Lỗi khi tải tất cả sản phẩm:", error);
      throw error; // Ném lỗi để hàm cha xử lý
    }
  };

  $scope.fetchTop5Items = async function () {
    try {
      const response = await $http.get(`http://localhost:8080/statistical/top5items`);
      const top5Items = response.data;
      $scope.top5Items = top5Items;
      console.log("Dữ liệu top 5 sản phẩm đã tải:", $scope.top5Items);
      $scope.drawBarChart(top5Items); // Vẽ biểu đồ sau khi tải dữ liệu
    } catch (error) {
      console.error("Lỗi khi tải top 5 sản phẩm:", error);
      throw error; // Ném lỗi để hàm cha xử lý
    }
  };

// Gọi hàm chính
  $scope.loadData();

  $scope.calculatePercentage = function (salesData, totalSales) {
    return salesData.map((item) =>
        ((item / totalSales) * 100).toFixed(2)
    ); // Hàm tính tỉ lệ phần trăm
  };

  $scope.drawBarChart = function (topItems) {
    if (!$scope.allSanPham || $scope.allSanPham.length === 0) {
      console.error("Dữ liệu sản phẩm chưa được tải.");
      return;
    }

    const labels = topItems.map((item) => item[0]); // Tên sản phẩm
    const salesData = topItems.map((item) => item[2]); // Số lượng bán
    const allSalesData = $scope.allSanPham.map((item) => item[3]); // Số tất cả sp lượng bán

    const totalSales = allSalesData.reduce((sum, current) => sum + current, 0);
    const percentageData = $scope.calculatePercentage(salesData, totalSales); // Tính phần trăm

    const ctx = document.getElementById("barChart").getContext("2d");
    new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels,
        datasets: [
          {
            label: "Tỉ lệ phần trăm sản phẩm đã bán",
            data: percentageData,
            backgroundColor: [
              "rgba(255, 99, 132, 0.2)",
              "rgba(255, 159, 64, 0.2)",
              "rgba(255, 205, 86, 0.2)",
              "rgba(75, 192, 192, 0.2)",
              "rgba(54, 162, 235, 0.2)"
            ],
            borderColor: [
              "rgb(255, 99, 132)",
              "rgb(255, 159, 64)",
              "rgb(255, 205, 86)",
              "rgb(75, 192, 192)",
              "rgb(54, 162, 235)"
            ],
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: true,
            position: "top"
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: "Sản phẩm"
            }
          },
          y: {
            title: {
              display: true,
              text: "Tỉ lệ phần trăm (%)"
            },
            beginAtZero: true,
            ticks: {
              max: 100
            }
          }
        }
      }
    });
  };

// Gọi hàm fetch dữ liệu
//   $scope.fetchAllSanPham();
//   $scope.fetchTop5Items();

  $scope.top5buyer = function () {
    $http
      .get(`http://localhost:8080/statistical/top5buyer`)
      .then((resp) => {
        $scope.top5buyer = resp.data;
        const labels = resp.data.map((item) => `${item[1]} (${item[0]})`);
        const data = resp.data.map((item) => item[3]);

        const ctx = document
          .getElementById("top5ChartBuyerChart")
          .getContext("2d");
        new Chart(ctx, {
          type: "line",
          data: {
            labels: labels,
            datasets: [
              {
                label: "Top 5 người mua nhiều nhất",
                data: data,
                backgroundColor: "rgba(75, 192, 192, 0.2)",
                borderColor: "rgba(75, 192, 192, 1)",
                borderWidth: 1
              }
            ]
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                position: "top"
              }
            },
            scales: {
              y: {
                beginAtZero: true
              }
            }
          }
        });
      })
      .catch((error) => console.log("Error", error));
  };
  let turnoverChart = null; // Khai báo biến toàn cục để lưu biểu đồ

  $scope.drawTurnoverChart = function (data) {
    const ctx = document.getElementById("turnoverChart").getContext("2d");

    // Xử lý dữ liệu
    const labels = data.map((item) => item[0]); // Thời gian (ngày/tháng/năm)
    const turnoverData = data.map((item) => item[2]); // Tổng doanh thu
    const quantityData = data.map((item) => item[1]); // Số lượng đơn hàng

    // Nếu biểu đồ đã tồn tại, xoá nó trước khi vẽ mới
    if (turnoverChart) {
      turnoverChart.destroy();
    }

    // Vẽ biểu đồ mới
    turnoverChart = new Chart(ctx, {
      type: "line",
      data: {
        labels: labels,
        datasets: [
          {
            label: "Doanh thu (VND)",
            data: turnoverData,
            borderColor: "rgba(75, 192, 192, 1)",
            backgroundColor: "rgba(75, 192, 192, 0.2)",
            borderWidth: 2,
            yAxisID: "y" // Trục Y1
          },
          {
            label: "Số lượng đơn hàng",
            data: quantityData,
            borderColor: "rgba(255, 99, 132, 1)",
            backgroundColor: "rgba(255, 99, 132, 0.2)",
            borderWidth: 2,
            yAxisID: "y1" // Trục Y2
          }
        ]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            position: "top"
          }
        },
        scales: {
          x: {
            title: {
              display: true,
              text: "Thời gian"
            }
          },
          y: {
            title: {
              display: true,
              text: "Doanh thu (VND)"
            },
            beginAtZero: true,
            position: "left" // Trục Y1 nằm bên trái
          },
          y1: {
            title: {
              display: true,
              text: "Số lượng đơn hàng"
            },
            beginAtZero: true,
            position: "right", // Trục Y2 nằm bên phải
            grid: {
              drawOnChartArea: false // Ngăn chặn lưới chồng lặp
            }
          }
        }
      }
    });
  };

  $scope.turnover = function () {
    $http
      .get(`http://localhost:8080/statistical/turnoverday`)
      .then((resp) => {
        $scope.itemTurnover = resp.data;
        return resp.data;
      })
      .then((data) => {
        $scope.drawTurnoverChart(data); // Vẽ biểu đồ
      })
      .catch((error) => console.log("Error", error));
  };

  $scope.turnovermonth = function () {
    $http
      .get(`http://localhost:8080/statistical/turnovermonth`)
      .then((resp) => {
        $scope.itemTurnover = resp.data;
        $scope.drawTurnoverChart($scope.itemTurnover); // Vẽ biểu đồ
      })
      .catch((error) => console.log("Error", error));
  };

  $scope.turnoveryear = function () {
    $http
      .get(`http://localhost:8080/statistical/turnoveryear`)
      .then((resp) => {
        $scope.itemTurnover = resp.data;
        $scope.drawTurnoverChart($scope.itemTurnover); // Vẽ biểu đồ
      })
      .catch((error) => console.log("Error", error));
  };

  $scope.itemTurnover = [];
  $scope.confirms = [];
  $scope.turnovers = {
    page: 0,
    size: 5,
    get items() {
      var start = this.page * this.size;
      return $scope.itemTurnover.slice(start, start + this.size);
    },
    get count() {
      return Math.ceil((1.0 * $scope.itemTurnover.length) / this.size);
    },
    first() {
      this.page = 0;
    },
    prev() {
      this.page--;
      if (this.page < 0) {
        this.last();
      }
    },
    next() {
      this.page++;
      if (this.page >= this.count) {
        this.first();
      }
    },
    last() {
      this.page = this.count - 1;
    }
  };
  $scope.listConfirm = {
    page: 0,
    size: 3,
    get items() {
      var start = this.page * this.size;
      return $scope.confirm.slice(start, start + this.size);
    },
    get count() {
      return Math.ceil((1.0 * $scope.confirm.length) / this.size);
    },
    first() {
      this.page = 0;
    },
    prev() {
      this.page--;
      if (this.page < 0) {
        this.last();
      }
    },
    next() {
      this.page++;
      if (this.page >= this.count) {
        this.first();
      }
    },
    last() {
      this.page = this.count - 1;
    }
  };

  $scope.Confirms = {
    page: 0,
    size: 5,
    get items() {
      var start = this.page * this.size;
      return $scope.confirms.slice(start, start + this.size);
    },
    get count() {
      return Math.ceil((1.0 * $scope.confirms.length) / this.size);
    },
    first() {
      this.page = 0;
    },
    prev() {
      this.page--;
      if (this.page < 0) {
        this.last();
      }
    },
    next() {
      this.page++;
      if (this.page >= this.count) {
        this.first();
      }
    },
    last() {
      this.page = this.count - 1;
    }
  };

  $scope.load_all();
  $scope.load();
  $scope.turnover();
  $scope.fetchAllSanPham();
  $scope.fetchTop5Items();
  $scope.top5buyer();
});
