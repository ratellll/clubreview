// 주소 받고 위도경도로 넣는것
function initAddressSearch(buttonId, addressFieldId, latitudeFieldId, longitudeFieldId) {
    document.getElementById(buttonId).addEventListener('click', function () {
        new daum.Postcode({
            oncomplete: function (data) {
                document.getElementById(addressFieldId).value = data.address;

                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(data.address, function (result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        document.getElementById(latitudeFieldId).value = result[0].y;
                        document.getElementById(longitudeFieldId).value = result[0].x;
                    } else {
                        alert('주소를 변환하는 데 실패했습니다.');
                    }
                });
            }
        }).open();
    });
}
