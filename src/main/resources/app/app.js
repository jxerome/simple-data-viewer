/*
 * Simple Data Viewer.
 */

var simpleDataViewer = angular.module('simpleDataViewer', []);


simpleDataViewer.controller('dataViewerCtrl', ['$scope', 'dataService', '$log', function($scope, dataService, $log) {
    $scope.tables = [];
    $scope.selectedTable = null;
    $scope.variables = [];
    $scope.selectedVariable = null;
    $scope.values = [];
    $scope.selectedValue = null;
    $scope.stats = null;
    $scope.message = 'Waiting for data...';

    dataService.listTables().then(
        function(response) {
            $scope.tables = response.data.tables;
            $scope.selectedTable = $scope.tables.length > 0 ? $scope.tables[0] : null;
        },
        function(response) {
            $log.error('Cannot load tables :' + response.code);
        }
    );

    $scope.$watch('selectedTable', function (table) {
        $scope.variables = [];
        $scope.selectedVariable = null;
        $scope.values = [];
        $scope.selectedValue = null;
        $scope.stats = null;

        if (table) {
            dataService.listVariables(table).then(
                function (response) {
                    $scope.variables = response.data.columns;
                    $scope.selectedVariable = $scope.variables.length > 0 ? $scope.variables[0] : null;
                },
                function (response) {
                    $log.error('Cannot load variables: ' + response.code);
                }
            );
            dataService.listValues(table).then(
                function (response) {
                    $scope.values = response.data.columns;
                    $scope.selectedValue = $scope.values.length > 0 ? $scope.values[0] : null;
                },
                function (response) {
                    $log.error('Cannot load values: ' + response.code);
                }
            );
        }
    });

    $scope.$watchGroup(['selectedVariable', 'selectedValue'], function(values) {
        if ($scope.selectedVariable && $scope.selectedValue){
            $scope.stats = null;
            $scope.message = 'Waiting for data...';

            dataService.loadStats($scope.selectedTable, $scope.selectedVariable, $scope.selectedValue).then(
                function (response) {
                    $scope.stats = response.data;
                    if ($scope.hasData($scope.stats.lines)) {
                        $scope.message = null;
                    } else {
                        $scope.message = 'This table is empty';
                    }
                },
                function (response) {
                    $scope.message = 'Error while loading data';
                    $log.error('Cannot load stats: ' + response.code);
                }
            );
        }
    });

    $scope.tableLabel = function (table) {
        return table ? table.name + " - " + table.file : "";
    };

    $scope.columnLabel = function(column) {
        return column ? column.name : null;
    };

    $scope.variableValueLabel = function(value) {
        return angular.isDefined(value) ? value : "(null)";
    };

    $scope.hasData = function(array) {
        return angular.isDefined(array) && array.length > 0;
    };
}]);


simpleDataViewer.factory('dataService', ['$http', function($http) {
    return {
        listTables : function() {
            return $http.get("/table");
        },
        listVariables: function(table) {
            return $http.get("/table/" + table.id + "/variable");
        },
        listValues: function(table) {
            return $http.get("/table/" + table.id + "/value");
        },
        loadStats: function(table, variable, value) {
            return $http.get("/table/" + table.id + "/variable/" + variable.name + "/value/" + value.name + "/stats");
        }
    };
}]);
