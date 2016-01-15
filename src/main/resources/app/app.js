/*
 * Simple Data Viewer.
 */

var simpleDataViewer = angular.module('simpleDataViewer', []);


simpleDataViewer.controller('dataViewerCtrl', ['$scope', 'dataService', '$log', function($scope, dataService, $log) {
    $scope.tables = [];
    $scope.selectedTable = null;
    $scope.variables = [];
    $scope.selectedVariable = null;

    dataService.listTables().then(
        function(response) {
            $scope.tables = response.data.tables;
            $scope.selectedTable = $scope.tables.length > 0 ? $scope.tables[0] : null;
        },
        function(response) {
            $log.error('Cannot load tables : ' + response.code);
        }
    );

    $scope.$watch('selectedTable', function (table, oldTable) {
        if (table) {
            dataService.listVariables(table).then(
                function (response) {
                    $scope.variables = response.data.columns;
                    $scope.selectedVariable = $scope.variables.length > 0 ? $scope.variables[0] : null;
                },
                function (response) {
                    $scope.variables = [];
                    $scope.selectedVariable = null;
                    $log.error('Cannot load variables : ' + response.code);
                }
            );
        } else {
            $scope.variables = [];
            $scope.selectedVariable = null;
        }
    });

    $scope.tableLabel = function (table) {
        return table ? table.name + " - " + table.file : "";
    };

    $scope.columnLabel = function(column) {
        return column ? column.name : null;
    };

}]);


simpleDataViewer.factory('dataService', ['$http', function($http) {
    return {
        listTables : function() {
            return $http.get("/table");
        },
        listVariables: function(table) {
            return $http.get("/table/" + table.id + "/variable");
        }
    };
}]);
