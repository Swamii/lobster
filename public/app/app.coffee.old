'use strict'

app = angular.module 'app', ['ui.router', 'api']

app.config ['$stateProvider', '$urlRouterProvider', ($stateProvider, $urlRouterProvider) ->
  $urlRouterProvider.otherwise('/dude')

  $stateProvider
  .state 'dude',
      url: '/dude'
      templateUrl: '/assets/angular/dude-list.html'
      controller: 'DudeCtrl'
      resolve:
        dudes: (Dude) ->
          Dude.query()
  .state 'dude.detail',
      url: '/{id:[0-9]+}'
      templateUrl: 'assets/angular/dude-detail.html'
      controller: 'DudeDetailCtrl'
      resolve:
        dude: ($stateParams, Dude) ->
          x = Dude.get(id: $stateParams.id)
          return x.$promise
        dudes: (dudes) ->
          dudes
]

app.controller 'DudeCtrl', ['$scope', 'Dude', 'SubService', 'dudes', ($scope, Dude, SubService, dudes) ->
  $scope.dudes = dudes
  $scope.newDude = new Dude()

  SubService.subscribe '/dudeChanged', (oldDude, repl) ->
    # subscribe to dude change events - needed if the list will be modified from outside this $scope
    targetIdx = -1
    angular.forEach $scope.dudes, (dude, idx) ->
      if dude.id is oldDude.id
        targetIdx = idx
    if repl
      $scope.dudes[targetIdx] = repl
    else
      $scope.dudes.splice(targetIdx, 1)

  $scope.processForm = ->
    $scope.newDude.$save (dude) ->
      $scope.dudes.push dude
    .then ->
        $scope.newDude = new Dude()

  $scope.selectDude = (selectedDude) ->
    angular.forEach $scope.dudes, (dude) ->
      if dude is selectedDude
        dude.selected = true
]

app.controller 'DudeDetailCtrl', ['$scope', '$state', 'SubService', 'dude', 'Dude', ($scope, $state, SubService, dude, Dude) ->
  $scope.isEdit = false
  $scope.dude = dude.dude
  $scope.editDude = new Dude()

  $scope.deleteDude = (dudeToDelete) ->
    Dude.delete id: dudeToDelete.id, ->
      # publish the deleted dude to update DudeCtrl.$scope.dudes
      SubService.publish('/dudeChanged', [dudeToDelete])
      $state.go 'dude'

  $scope.openForEdit = ->
    $scope.isEdit = true
    $scope.editDude = angular.copy $scope.dude

  $scope.closeEdit = ->
    $scope.isEdit = false
    $scope.editDude = new Dude()

  $scope.updateDude = ->
    editDude = new Dude($scope.editDude)
    editDude.$update (dude) ->
      SubService.publish('/dudeChanged', [$scope.dude, dude])
      $scope.dude = dude
      $scope.closeEdit()
]


###
  # API #

  'use strict'

  api = angular.module 'api', ['ngResource']

  api.factory 'Dude', ['$resource', ($resource) ->
    $resource 'api/dude/:id', id: '@id',
      update: method: 'PUT'
  ]

  api.factory 'SubService', ->
    cache = {}

    publish: (topic, args) ->
      cache[topic] and angular.forEach cache[topic], (func) ->
        func.apply null, args or []
    subscribe: (topic, callback) ->
      cache[topic] = [] if not cache[topic]
      cache[topic].push callback
      [topic, callback]
    unsubscribe: (handle) ->
      t = handle[0]
      cache[t] and angular.forEach cache[t] (val, idx) ->
        cache[t].splice idx, 1 if val is handle[1]
###