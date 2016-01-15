/*
 * Simple Data Viewer.
 */

var simpleDataViewer = angular.module('simpleDataViewer', []);


simpleDataViewer.controller('dataViewerCtrl', ['$scope', 'dataService', '$log', function($scope, dataService, $log) {
    dataService.listTables().then(
        function(response) {
            $scope.tables = response.data.tables;
            if ($scope.tables.length > 0) {
                $scope.selectedTable = $scope.tables[0];
            }
        },
        function(response) {
            $scope.tables = [];
            $scope.selectedTable = null;
            $log.error('Cannot load tables : ' + response.code);
        }
    );

    $scope.tableLabel = function (table) {
        return table ? table.name + " - " + table.file : "";
    };

}]);


simpleDataViewer.factory('dataService', ['$http', function($http) {
    return {
        listTables : function() {
            return $http.get("/table");
        }
    };
}]);
