app = angular.module 'app.utils', []

app.factory 'Utils', ->
    indexById: (iterable, checkObj) ->
        index = -1
        angular.forEach iterable, (obj, i) ->
            index = i if obj.id is checkObj.id
    fromId: (iterable, checkId) ->
        the_obj = {}
        angular.forEach iterable, (obj) ->
            the_obj = obj if obj.id is checkId
    chunkIt: (iterable, cols) ->
      i for i in [0..iterable.length] by cols